package com.rikkeibank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public final class AccountDtos {
    private AccountDtos() {}

    @Schema(description = "Customer account balance view")
    public record AccountResponse(Long id, String accountNumber, BigDecimal balance, String currency, boolean active) {}

    @Schema(description = "Create account request for admin or staff")
    public record CreateAccountRequest(
        Long userId,
        @NotBlank @Pattern(regexp="\\d{6}") String transactionPin,
        String currency
    ) {}

    @Schema(description = "Update account status or currency request")
    public record UpdateAccountRequest(Boolean active, String currency) {}

    @Schema(description = "Admin account management view")
    public record AdminAccountResponse(Long id, Long userId, String username, String accountNumber,
        BigDecimal balance, String currency, boolean active) {}
}
