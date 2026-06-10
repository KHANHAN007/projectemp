package com.rikkeibank.dto;

import com.rikkeibank.domain.RoleName;

import java.time.LocalDateTime;

public record UserResponse(Long id, String username, String email, String phoneNumber,
                           RoleName role, boolean active, boolean kyc, LocalDateTime createdAt) {}
