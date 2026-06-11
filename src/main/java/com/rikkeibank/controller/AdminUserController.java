package com.rikkeibank.controller;

import com.rikkeibank.dto.ApiResponse;
import com.rikkeibank.dto.UserDtos.UpdateRequest;
import com.rikkeibank.dto.UserResponse;
import com.rikkeibank.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "FR-05 User Management", description = "User CRUD and pagination APIs")
public class AdminUserController {
    private final UserService service;

    public AdminUserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List users with pagination")
    public ApiResponse<Page<UserResponse>> list(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.ok("Users retrieved", service.list(pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user")
    public ApiResponse<UserResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody UpdateRequest request
    ) {
        return ApiResponse.ok("User updated", service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a user")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
