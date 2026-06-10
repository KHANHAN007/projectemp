package com.rikkeibank.dto;

import com.rikkeibank.domain.KycStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public final class KycDtos {
    private KycDtos() {}
    public record KycRequest(@NotBlank String idNumber, @NotBlank String fullName,
        @NotNull @Past LocalDate dob, @NotBlank String sex, @NotBlank String address) {}
    public record ReviewRequest(@NotNull KycStatus status, String reason) {}
    public record KycResponse(Long id, String username, String idNumber, String fullName,
        String imageUrl, KycStatus status, String rejectionReason) {}
}
