package com.rikkeibank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public final class AccountDtos {
    private AccountDtos() {}
    public record AccountResponse(Long id, String accountNumber, BigDecimal balance, String currency, boolean active) {}
    public record CreateAccountRequest(Long userId, @NotBlank @Pattern(regexp="\\d{6}") String transactionPin, String currency) {}
    public record UpdateAccountRequest(Boolean active, String currency) {}
    public record AdminAccountResponse(Long id, Long userId, String username, String accountNumber,
        BigDecimal balance, String currency, boolean active) {}
}
