package com.rikkeibank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class TransactionDtos {
    private TransactionDtos() {}
    public record TransferRequest(@NotNull Long fromAccountId, Long toAccountId, String beneficiaryBank,
        String beneficiaryAccount, @NotNull @DecimalMin("0.01") BigDecimal amount,
        @Size(max=255) String description, @NotBlank String pin) {}
    public record TransactionResponse(String transactionCode, Long fromAccountId, Long toAccountId,
        BigDecimal amount, String description, String type, String status, String direction, LocalDateTime createdAt) {}
}
