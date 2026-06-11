package com.rikkeibank.controller;

import com.rikkeibank.dto.AccountDtos.AdminAccountResponse;
import com.rikkeibank.dto.AccountDtos.CreateAccountRequest;
import com.rikkeibank.dto.AccountDtos.UpdateAccountRequest;
import com.rikkeibank.dto.ApiResponse;
import com.rikkeibank.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/admin/accounts", "/api/v1/staff/accounts"})
@Tag(name = "FR-05 Account Management", description = "Account CRUD and pagination APIs")
public class AdminAccountController {
    private final AccountService service;

    public AdminAccountController(AccountService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List accounts with pagination")
    public ApiResponse<Page<AdminAccountResponse>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok("Accounts retrieved", service.list(pageable));
    }

    @PostMapping
    @Operation(summary = "Create a bank account for a user")
    public ResponseEntity<ApiResponse<AdminAccountResponse>> create(
        @Valid @RequestBody CreateAccountRequest request
    ) {
        AdminAccountResponse response = service.create(
            request.userId(),
            request.transactionPin(),
            request.currency()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Account created", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update account status or currency")
    public ApiResponse<AdminAccountResponse> update(
        @PathVariable Long id,
        @RequestBody UpdateAccountRequest request
    ) {
        return ApiResponse.ok("Account updated", service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate an account")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
