package com.rikkeibank.controller;

import com.rikkeibank.dto.ApiResponse;
import com.rikkeibank.dto.KycDtos.KycRequest;
import com.rikkeibank.dto.KycDtos.KycResponse;
import com.rikkeibank.dto.KycDtos.ReviewRequest;
import com.rikkeibank.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "FR-04/09 eKYC", description = "Customer eKYC upload and staff review APIs")
public class KycController {
    private final KycService service;

    public KycController(KycService service) {
        this.service = service;
    }

    @PostMapping(value = "/api/v1/customer/kyc", consumes = "multipart/form-data")
    @Operation(summary = "Submit customer eKYC profile with uploaded document")
    public ApiResponse<KycResponse> submit(
        @Valid @RequestPart("data") KycRequest request,
        @RequestPart("file") MultipartFile file,
        Authentication authentication
    ) {
        return ApiResponse.ok("KYC submitted", service.submit(authentication.getName(), request, file));
    }

    @GetMapping("/api/v1/staff/kyc")
    @Operation(summary = "List pending eKYC profiles")
    public ApiResponse<Page<KycResponse>> pending(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok("Pending KYC retrieved", service.pending(pageable));
    }

    @PutMapping("/api/v1/staff/kyc/{id}/review")
    @Operation(summary = "Approve or reject an eKYC profile")
    public ApiResponse<KycResponse> review(
        @PathVariable Long id,
        @Valid @RequestBody ReviewRequest request,
        Authentication authentication
    ) {
        return ApiResponse.ok("KYC reviewed", service.review(id, authentication.getName(), request));
    }
}
