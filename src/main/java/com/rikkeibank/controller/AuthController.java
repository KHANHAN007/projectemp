package com.rikkeibank.controller;

import com.rikkeibank.dto.ApiResponse;
import com.rikkeibank.dto.AuthDtos.*;
import com.rikkeibank.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/auth", "/api/v1/auth"})
public class AuthController {
    private final AuthService service;
    private final com.rikkeibank.service.PasswordResetService resets;
    public AuthController(AuthService service,com.rikkeibank.service.PasswordResetService resets){this.service=service;this.resets=resets;}
    @PostMapping("/login") public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest req){return ApiResponse.ok("Login successful",service.login(req));}
    @PostMapping("/refresh") public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest req){return ApiResponse.ok("Token rotated",service.refresh(req.refreshToken()));}
    @PostMapping("/logout") public ResponseEntity<Void> logout(@RequestHeader("Authorization")String auth, Authentication principal){
        if(auth==null||!auth.startsWith("Bearer "))throw new com.rikkeibank.exception.BusinessException(org.springframework.http.HttpStatus.UNAUTHORIZED,"Bearer token is required");
        service.logout(auth.substring(7),principal.getName());return ResponseEntity.ok().build();
    }
    @PutMapping("/change-password") public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req, Authentication principal){service.changePassword(principal.getName(),req);return ApiResponse.ok("Password changed",null);}
    @PostMapping("/forgot-password") public ApiResponse<String> forgot(@Valid @RequestBody ForgotPasswordRequest req){return ApiResponse.ok("Reset token issued for demo delivery",resets.request(req));}
    @PostMapping("/reset-password") public ApiResponse<Void> reset(@Valid @RequestBody ResetPasswordRequest req){resets.reset(req);return ApiResponse.ok("Password reset",null);}
}
