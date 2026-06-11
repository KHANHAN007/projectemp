package com.rikkeibank.service;

import com.rikkeibank.dto.AuthDtos.ForgotPasswordRequest;
import com.rikkeibank.dto.AuthDtos.ResetPasswordRequest;
import com.rikkeibank.exception.BusinessException;
import com.rikkeibank.repository.RefreshTokenRepository;
import com.rikkeibank.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
public class PasswordResetService {
    private static final String PREFIX = "password-reset:";

    private final UserRepository users;
    private final RefreshTokenRepository tokens;
    private final StringRedisTemplate redis;
    private final PasswordEncoder encoder;

    public PasswordResetService(
        UserRepository users,
        RefreshTokenRepository tokens,
        StringRedisTemplate redis,
        PasswordEncoder encoder
    ) {
        this.users = users;
        this.tokens = tokens;
        this.redis = redis;
        this.encoder = encoder;
    }

    public String request(ForgotPasswordRequest request) {
        var user = users.findByUsername(request.username())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));

        String token = UUID.randomUUID().toString();
        redis.opsForValue().set(PREFIX + token, user.getUsername(), Duration.ofMinutes(15));
        log.info("Issued password reset token username={}", user.getUsername());
        return token;
    }

    @Transactional
    public void reset(ResetPasswordRequest request) {
        String username = redis.opsForValue().get(PREFIX + request.token());
        if (username == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid or expired reset token");
        }

        users.findByUsername(username).ifPresent(user -> {
            user.setPassword(encoder.encode(request.newPassword()));
            tokens.deleteByUserId(user.getId());
        });
        redis.delete(PREFIX + request.token());
        log.info("Reset password and revoked refresh tokens username={}", username);
    }
}
