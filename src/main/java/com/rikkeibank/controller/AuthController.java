package com.rikkeibank.controller;

import com.rikkeibank.dto.ApiResponse;
import com.rikkeibank.dto.AuthDtos.ChangePasswordRequest;
import com.rikkeibank.dto.AuthDtos.ForgotPasswordRequest;
import com.rikkeibank.dto.AuthDtos.LoginRequest;
import com.rikkeibank.dto.AuthDtos.RefreshRequest;
import com.rikkeibank.dto.AuthDtos.ResetPasswordRequest;
import com.rikkeibank.dto.AuthDtos.TokenResponse;
import com.rikkeibank.exception.BusinessException;
import com.rikkeibank.service.AuthService;
import com.rikkeibank.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/auth", "/api/v1/auth"})
@Tag(name = "FR-01/02/03/10 Authentication", description = "JWT, refresh token, logout and password APIs")
public class AuthController {
    private final AuthService service;
    private final PasswordResetService resets;

    public AuthController(AuthService service, PasswordResetService resets) {
        this.service = service;
        this.resets = resets;
    }

    @PostMapping("/login")
    @Operation(summary = "Login and issue JWT access token plus refresh token")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Login successful", service.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate refresh token and issue new JWT")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token rotated")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.ok("Token rotated", service.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke current JWT")
    public ResponseEntity<Void> logout(
        @RequestHeader("Authorization") String authorization,
        Authentication principal
    ) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Bearer token is required");
        }
        service.logout(authorization.substring(7), principal.getName());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change current login password")
    public ApiResponse<Void> changePassword(
        @Valid @RequestBody ChangePasswordRequest request,
        Authentication principal
    ) {
        service.changePassword(principal.getName(), request);
        return ApiResponse.ok("Password changed", null);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Issue a demo password reset token")
    public ApiResponse<String> forgot(@Valid @RequestBody ForgotPasswordRequest request) {
        return ApiResponse.ok("Reset token issued for demo delivery", resets.request(request));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset login password with a reset token")
    public ApiResponse<Void> reset(@Valid @RequestBody ResetPasswordRequest request) {
        resets.reset(request);
        return ApiResponse.ok("Password reset", null);
    }
}
