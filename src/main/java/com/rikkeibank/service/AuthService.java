package com.rikkeibank.service;

import com.rikkeibank.model.RefreshToken;
import com.rikkeibank.model.User;
import com.rikkeibank.dto.AuthDtos.ChangePasswordRequest;
import com.rikkeibank.dto.AuthDtos.LoginRequest;
import com.rikkeibank.dto.AuthDtos.TokenResponse;
import com.rikkeibank.exception.BusinessException;
import com.rikkeibank.repository.RefreshTokenRepository;
import com.rikkeibank.repository.UserRepository;
import com.rikkeibank.security.JwtService;
import com.rikkeibank.security.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
@Slf4j
public class AuthService {
    private final AuthenticationManager auth;
    private final UserRepository users;
    private final RefreshTokenRepository tokens;
    private final JwtService jwt;
    private final TokenBlacklistService blacklist;
    private final PasswordEncoder encoder;
    private final long refreshExpiration;

    public AuthService(
        AuthenticationManager auth,
        UserRepository users,
        RefreshTokenRepository tokens,
        JwtService jwt,
        TokenBlacklistService blacklist,
        PasswordEncoder encoder,
        @Value("${app.jwt.refresh-expiration}") long refreshExpiration
    ) {
        this.auth = auth;
        this.users = users;
        this.tokens = tokens;
        this.jwt = jwt;
        this.blacklist = blacklist;
        this.encoder = encoder;
        this.refreshExpiration = refreshExpiration;
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        try {
            auth.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (DisabledException e) {
            log.warn("Rejected login for locked account username={}", request.username());
            throw new BusinessException(HttpStatus.FORBIDDEN, "Account is locked");
        } catch (AuthenticationException e) {
            log.warn("Rejected login for invalid credentials username={}", request.username());
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        User user = users.findByUsername(request.username()).orElseThrow();
        log.info("Login successful username={}", request.username());
        return issue(user);
    }

    @Transactional
    public TokenResponse refresh(String raw) {
        RefreshToken oldToken = tokens.findByTokenHash(hash(raw))
            .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (oldToken.isRevoked() || oldToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Rejected expired or revoked refresh token id={}", oldToken.getId());
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Expired or revoked refresh token");
        }

        oldToken.setRevoked(true);
        log.info("Rotated refresh token id={} userId={}", oldToken.getId(), oldToken.getUser().getId());
        return issue(oldToken.getUser());
    }

    @Transactional
    public void logout(String accessToken, String username) {
        Claims claims = jwt.parse(accessToken);
        blacklist.revoke(claims.getId(), jwt.remainingMillis(accessToken));
        users.findByUsername(username).ifPresent(user -> tokens.deleteByUserId(user.getId()));
        log.info("Logged out username={} jti={}", username, claims.getId());
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = users.findByUsername(username)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));

        if (!encoder.matches(request.currentPassword(), user.getPassword())) {
            log.warn("Rejected password change username={}", username);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }

        user.setPassword(encoder.encode(request.newPassword()));
        tokens.deleteByUserId(user.getId());
        log.info("Changed password and revoked refresh tokens username={}", username);
    }

    private TokenResponse issue(User user) {
        String rawRefreshToken = UUID.randomUUID() + "." + UUID.randomUUID();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hash(rawRefreshToken));
        refreshToken.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshExpiration)));
        refreshToken.setCreatedAt(LocalDateTime.now());
        tokens.save(refreshToken);

        return new TokenResponse(
            jwt.createAccessToken(user),
            rawRefreshToken,
            "Bearer",
            jwt.getAccessExpirationSeconds()
        );
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
