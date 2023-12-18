package prime.prime.domain.account.models;

import prime.prime.domain.role.Role;
import prime.prime.infrastructure.validation.EnumValidation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountCreateDto(
        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email should be valid")
        String email,

        @NotNull(message = "Role is mandatory")
        @EnumValidation(value = Role.class)
        String role
        ) {
}
