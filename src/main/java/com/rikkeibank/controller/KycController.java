package com.rikkeibank.controller;

import com.rikkeibank.dto.ApiResponse;
import com.rikkeibank.dto.KycDtos.KycRequest;
import com.rikkeibank.dto.KycDtos.KycResponse;
import com.rikkeibank.dto.KycDtos.ReviewRequest;
import com.rikkeibank.service.KycService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class KycController {
    private final KycService service;public KycController(KycService service){this.service=service;}
    @PostMapping(value="/api/v1/customer/kyc",consumes="multipart/form-data")
    public ApiResponse<KycResponse> submit(@Valid @RequestPart("data")KycRequest req,@RequestPart("file")MultipartFile file,Authentication a){return ApiResponse.ok("KYC submitted",service.submit(a.getName(),req,file));}
    @GetMapping("/api/v1/staff/kyc") public ApiResponse<Page<KycResponse>> pending(@PageableDefault(size=20)Pageable p){return ApiResponse.ok("Pending KYC retrieved",service.pending(p));}
    @PutMapping("/api/v1/staff/kyc/{id}/review") public ApiResponse<KycResponse> review(@PathVariable Long id,@Valid @RequestBody ReviewRequest req,Authentication a){return ApiResponse.ok("KYC reviewed",service.review(id,a.getName(),req));}
}
