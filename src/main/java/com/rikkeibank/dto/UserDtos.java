package com.rikkeibank.dto;

import com.rikkeibank.model.RoleName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public final class UserDtos {
    private UserDtos() {}

    @Schema(description = "Customer registration and initial account opening request")
    public record RegisterRequest(@NotBlank @Size(min=4,max=50) String username,
        @NotBlank @Size(min=8,max=72) String password, @NotBlank @Email String email,
        @NotBlank String phoneNumber, @NotBlank @Pattern(regexp="\\d{6}") String transactionPin) {}

    @Schema(description = "Admin user update request")
    public record UpdateRequest(@Email String email, String phoneNumber, Boolean active, RoleName role) {}

    @Schema(description = "Change transaction PIN request")
    public record ChangePinRequest(@NotBlank String currentPin,
        @NotBlank @Pattern(regexp="\\d{6}") String newPin) {}
}
