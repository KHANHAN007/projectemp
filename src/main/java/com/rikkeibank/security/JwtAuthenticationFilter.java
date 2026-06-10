package com.rikkeibank.security;

import com.rikkeibank.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final TokenBlacklistService blacklist;
    private final UserRepository users;
    public JwtAuthenticationFilter(JwtService jwt, TokenBlacklistService blacklist, UserRepository users) {
        this.jwt=jwt; this.blacklist=blacklist; this.users=users;
    }
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
        throws ServletException, IOException {
        String header=req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims c=jwt.parse(header.substring(7));
                if (!blacklist.isRevoked(c.getId())) users.findByUsername(c.getSubject()).filter(u -> u.isActive()).ifPresent(u ->
                    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                        u.getUsername(), null, List.of(new SimpleGrantedAuthority("ROLE_"+u.getRole().getName().name())))));
            } catch (JwtException | IllegalArgumentException e) {
                log.debug("Rejected invalid or expired JWT: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            } catch (RedisConnectionFailureException e) {
                log.error("Redis is unavailable while checking the JWT blacklist", e);
                res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Token revocation service is unavailable");
                return;
            }
        }
        chain.doFilter(req,res);
    }
}
