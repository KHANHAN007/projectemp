package com.rikkeibank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {}
    public record ChangePasswordRequest(@NotBlank String currentPassword,
        @NotBlank @Size(min = 8, max = 72) String newPassword) {}
    public record ForgotPasswordRequest(@NotBlank String username) {}
    public record ResetPasswordRequest(@NotBlank String token,
        @NotBlank @Size(min = 8, max = 72) String newPassword) {}
}
