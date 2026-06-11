package com.rikkeibank.security;

import com.rikkeibank.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private final SecretKey key;
    private final long accessExpiration;
    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.access-expiration}") long accessExpiration) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessExpiration = accessExpiration;
    }
    public String createAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder().id(UUID.randomUUID().toString()).subject(user.getUsername())
            .claim("role", user.getRole().getName().name()).issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(accessExpiration))).signWith(key).compact();
    }
    public Claims parse(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); }
    public long remainingMillis(String token) { return Math.max(1, parse(token).getExpiration().getTime() - System.currentTimeMillis()); }
    public long getAccessExpirationSeconds() { return accessExpiration / 1000; }
}
