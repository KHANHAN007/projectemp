package com.rikkeibank.controller;

import com.rikkeibank.dto.ApiResponse;
import com.rikkeibank.dto.UserDtos.RegisterRequest;
import com.rikkeibank.dto.UserResponse;
import com.rikkeibank.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/registrations")
public class RegistrationController {
    private final UserService service;public RegistrationController(UserService service){this.service=service;}
    @PostMapping public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest req){return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Registration successful",service.register(req)));}
}
