package com.rikkeibank.controller;

import com.rikkeibank.dto.ApiResponse;
import com.rikkeibank.dto.UserDtos.UpdateRequest;
import com.rikkeibank.dto.UserResponse;
import com.rikkeibank.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {
    private final UserService service;public AdminUserController(UserService service){this.service=service;}
    @GetMapping public ApiResponse<Page<UserResponse>> list(@PageableDefault(size=20,sort="createdAt",direction=Sort.Direction.DESC)Pageable p){return ApiResponse.ok("Users retrieved",service.list(p));}
    @PutMapping("/{id}") public ApiResponse<UserResponse> update(@PathVariable Long id,@Valid @RequestBody UpdateRequest req){return ApiResponse.ok("User updated",service.update(id,req));}
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){service.delete(id);return ResponseEntity.noContent().build();}
}
