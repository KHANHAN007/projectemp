package com.rikkeibank.dto;

import com.rikkeibank.domain.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class UserDtos {
    private UserDtos() {}
    public record RegisterRequest(@NotBlank @Size(min=4,max=50) String username,
        @NotBlank @Size(min=8,max=72) String password, @NotBlank @Email String email,
        @NotBlank String phoneNumber, @NotBlank @Pattern(regexp="\\d{6}") String transactionPin) {}
    public record UpdateRequest(@Email String email, String phoneNumber, Boolean active, RoleName role) {}
    public record ChangePinRequest(@NotBlank String currentPin,
        @NotBlank @Pattern(regexp="\\d{6}") String newPin) {}
}
