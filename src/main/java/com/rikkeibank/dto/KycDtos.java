package com.rikkeibank.dto;

import com.rikkeibank.model.KycStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public final class KycDtos {
    private KycDtos() {}

    @Schema(description = "Customer eKYC profile submission data")
    public record KycRequest(@NotBlank String idNumber, @NotBlank String fullName,
        @NotNull @Past LocalDate dob, @NotBlank String sex, @NotBlank String address) {}

    @Schema(description = "Staff/admin eKYC review request")
    public record ReviewRequest(@NotNull KycStatus status, String reason) {}

    @Schema(description = "eKYC profile response")
    public record KycResponse(Long id, String username, String idNumber, String fullName,
        String imageUrl, KycStatus status, String rejectionReason) {}
}
