package com.rikkeibank.controller;

import com.rikkeibank.dto.AccountDtos.AdminAccountResponse;
import com.rikkeibank.dto.AccountDtos.CreateAccountRequest;
import com.rikkeibank.dto.AccountDtos.UpdateAccountRequest;
import com.rikkeibank.dto.ApiResponse;
import com.rikkeibank.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/admin/accounts","/api/v1/staff/accounts"})
public class AdminAccountController {
    private final AccountService service;public AdminAccountController(AccountService service){this.service=service;}
    @GetMapping public ApiResponse<Page<AdminAccountResponse>> list(@PageableDefault(size=20)Pageable p){return ApiResponse.ok("Accounts retrieved",service.list(p));}
    @PostMapping public ResponseEntity<ApiResponse<AdminAccountResponse>> create(@Valid @RequestBody CreateAccountRequest req){return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Account created",service.create(req.userId(),req.transactionPin(),req.currency())));}
    @PutMapping("/{id}") public ApiResponse<AdminAccountResponse> update(@PathVariable Long id,@RequestBody UpdateAccountRequest req){return ApiResponse.ok("Account updated",service.update(id,req));}
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){service.delete(id);return ResponseEntity.noContent().build();}
}
