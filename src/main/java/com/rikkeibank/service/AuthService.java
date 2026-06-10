package com.rikkeibank.service;

import com.rikkeibank.domain.RefreshToken;
import com.rikkeibank.domain.User;
import com.rikkeibank.dto.AuthDtos.ChangePasswordRequest;
import com.rikkeibank.dto.AuthDtos.LoginRequest;
import com.rikkeibank.dto.AuthDtos.TokenResponse;
import com.rikkeibank.exception.BusinessException;
import com.rikkeibank.repository.RefreshTokenRepository;
import com.rikkeibank.repository.UserRepository;
import com.rikkeibank.security.JwtService;
import com.rikkeibank.security.TokenBlacklistService;
import io.jsonwebtoken.Claims;
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
public class AuthService {
    private final AuthenticationManager auth; private final UserRepository users; private final RefreshTokenRepository tokens;
    private final JwtService jwt; private final TokenBlacklistService blacklist; private final PasswordEncoder encoder; private final long refreshExpiration;
    public AuthService(AuthenticationManager auth, UserRepository users, RefreshTokenRepository tokens, JwtService jwt,
        TokenBlacklistService blacklist, PasswordEncoder encoder, @Value("${app.jwt.refresh-expiration}") long refreshExpiration) {
        this.auth=auth;this.users=users;this.tokens=tokens;this.jwt=jwt;this.blacklist=blacklist;this.encoder=encoder;this.refreshExpiration=refreshExpiration;
    }
    @Transactional public TokenResponse login(LoginRequest req) {
        try { auth.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password())); }
        catch (DisabledException e) { throw new BusinessException(HttpStatus.FORBIDDEN,"Account is locked"); }
        catch (AuthenticationException e) { throw new BusinessException(HttpStatus.UNAUTHORIZED,"Invalid credentials"); }
        return issue(users.findByUsername(req.username()).orElseThrow());
    }
    @Transactional public TokenResponse refresh(String raw) {
        RefreshToken old=tokens.findByTokenHash(hash(raw)).orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED,"Invalid refresh token"));
        if (old.isRevoked() || old.getExpiresAt().isBefore(LocalDateTime.now())) throw new BusinessException(HttpStatus.UNAUTHORIZED,"Expired or revoked refresh token");
        old.setRevoked(true); return issue(old.getUser());
    }
    @Transactional public void logout(String accessToken, String username) {
        Claims c=jwt.parse(accessToken); blacklist.revoke(c.getId(),jwt.remainingMillis(accessToken));
        users.findByUsername(username).ifPresent(u -> tokens.deleteByUserId(u.getId()));
    }
    @Transactional public void changePassword(String username, ChangePasswordRequest req) {
        User user=users.findByUsername(username).orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"User not found"));
        if(!encoder.matches(req.currentPassword(),user.getPassword()))throw new BusinessException(HttpStatus.UNAUTHORIZED,"Current password is incorrect");
        user.setPassword(encoder.encode(req.newPassword()));
        tokens.deleteByUserId(user.getId());
    }
    private TokenResponse issue(User user) {
        String raw=UUID.randomUUID()+"."+UUID.randomUUID();
        RefreshToken rt=new RefreshToken();rt.setUser(user);rt.setTokenHash(hash(raw));
        rt.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshExpiration)));rt.setCreatedAt(LocalDateTime.now());tokens.save(rt);
        return new TokenResponse(jwt.createAccessToken(user),raw,"Bearer",jwt.getAccessExpirationSeconds());
    }
    private String hash(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
}
