package com.rikkeibank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class TransactionDtos {
    private TransactionDtos() {}

    @Schema(description = "Internal or interbank transfer request")
    public record TransferRequest(@NotNull Long fromAccountId, Long toAccountId, String beneficiaryBank,
        String beneficiaryAccount, @NotNull @DecimalMin("0.01") BigDecimal amount,
        @Size(max=255) String description, @NotBlank String pin) {}

    @Schema(description = "Bank transaction response")
    public record TransactionResponse(String transactionCode, Long fromAccountId, Long toAccountId,
        BigDecimal amount, String description, String type, String status, String direction, LocalDateTime createdAt) {}
}
