package com.rikkeibank.config;

import com.rikkeibank.repository.UserRepository;
import com.rikkeibank.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpStatus;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
    @Bean UserDetailsService userDetailsService(UserRepository repo) {
        return username -> repo.findByUsername(username).map(u -> org.springframework.security.core.userdetails.User
            .withUsername(u.getUsername()).password(u.getPassword()).roles(u.getRole().getName().name())
            .disabled(!u.isActive()).build()).orElseThrow(() -> new UsernameNotFoundException(username));
    }
    @Bean AuthenticationProvider authenticationProvider(UserDetailsService uds, PasswordEncoder encoder) {
        DaoAuthenticationProvider p=new DaoAuthenticationProvider(uds); p.setPasswordEncoder(encoder); return p;
    }
    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception { return c.getAuthenticationManager(); }
    @Bean SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwt) throws Exception {
        return http.csrf(c->c.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e->e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authorizeHttpRequests(a->a
                .requestMatchers("/api/auth/login","/api/auth/refresh","/api/auth/forgot-password","/api/auth/reset-password",
                    "/api/v1/auth/login","/api/v1/auth/refresh","/api/v1/auth/forgot-password","/api/v1/auth/reset-password",
                    "/api/v1/registrations","/swagger-ui.html","/swagger-ui/**","/v3/api-docs/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/staff/**").hasAnyRole("ADMIN","STAFF")
                .requestMatchers("/api/v1/customer/**").hasRole("CUSTOMER")
                .anyRequest().authenticated())
            .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class).build();
    }
}
