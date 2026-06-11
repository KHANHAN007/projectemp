package com.rikkeibank.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService {
    private static final String PREFIX = "jwt:blacklist:";

    private final StringRedisTemplate redis;

    public TokenBlacklistService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void revoke(String jti, long ttlMillis) {
        redis.opsForValue().set(PREFIX + jti, "revoked", Duration.ofMillis(ttlMillis));
    }

    public boolean isRevoked(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(PREFIX + jti));
    }
}
