package com.rikkeibank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    @Schema(description = "Login request with username and password")
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    @Schema(description = "Refresh token rotation request")
    public record RefreshRequest(@NotBlank String refreshToken) {}

    @Schema(description = "JWT access token and refresh token response")
    public record TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {}

    @Schema(description = "Change current password request")
    public record ChangePasswordRequest(@NotBlank String currentPassword,
        @NotBlank @Size(min = 8, max = 72) String newPassword) {}

    @Schema(description = "Forgot password request")
    public record ForgotPasswordRequest(@NotBlank String username) {}

    @Schema(description = "Reset password with issued reset token request")
    public record ResetPasswordRequest(@NotBlank String token,
        @NotBlank @Size(min = 8, max = 72) String newPassword) {}
}
