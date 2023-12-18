package prime.prime.domain.account.models;

import prime.prime.domain.role.Role;
import prime.prime.infrastructure.validation.EnumValidation;


public record AccountUpdateDto(
        @EnumValidation(value = Role.class)
        String role
) {
}
