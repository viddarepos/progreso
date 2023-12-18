package prime.prime.domain.user.models;

import prime.prime.domain.account.entity.AccountStatus;
import prime.prime.domain.role.Role;
import prime.prime.infrastructure.validation.EnumValidation;

public record SearchUserDto(
    String fullName,

    String location,

    @EnumValidation(value = Role.class)
    String role,

    @EnumValidation(value = AccountStatus.class)
    String status,

    String season,

    String technology
) {

}
