package prime.prime.domain.account.models;

import prime.prime.infrastructure.password.validator.ValidPassword;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordDto(
        @NotBlank(message = "Old password is mandatory")
        String oldPassword,

        @ValidPassword
        @NotBlank(message = "New password is mandatory")
        String newPassword
) {
}
