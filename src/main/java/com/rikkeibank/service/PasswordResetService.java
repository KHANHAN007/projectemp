package com.rikkeibank.service;

import com.rikkeibank.dto.AuthDtos.ForgotPasswordRequest;
import com.rikkeibank.dto.AuthDtos.ResetPasswordRequest;
import com.rikkeibank.exception.BusinessException;
import com.rikkeibank.repository.RefreshTokenRepository;
import com.rikkeibank.repository.UserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
public class PasswordResetService {
    private static final String PREFIX="password-reset:";
    private final UserRepository users;private final RefreshTokenRepository tokens;private final StringRedisTemplate redis;private final PasswordEncoder encoder;
    public PasswordResetService(UserRepository users,RefreshTokenRepository tokens,StringRedisTemplate redis,PasswordEncoder encoder){this.users=users;this.tokens=tokens;this.redis=redis;this.encoder=encoder;}
    public String request(ForgotPasswordRequest req){
        var u=users.findByUsername(req.username()).orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"User not found"));
        String token=UUID.randomUUID().toString();redis.opsForValue().set(PREFIX+token,u.getUsername(),Duration.ofMinutes(15));return token;
    }
    @Transactional public void reset(ResetPasswordRequest req){
        String username=redis.opsForValue().get(PREFIX+req.token());if(username==null)throw new BusinessException(HttpStatus.UNAUTHORIZED,"Invalid or expired reset token");
        if(req.newPassword().length()<8)throw new BusinessException(HttpStatus.BAD_REQUEST,"Password must contain at least 8 characters");
        users.findByUsername(username).ifPresent(u->{u.setPassword(encoder.encode(req.newPassword()));tokens.deleteByUserId(u.getId());});redis.delete(PREFIX+req.token());
    }
}
