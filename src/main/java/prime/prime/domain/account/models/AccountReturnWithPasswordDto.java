package prime.prime.domain.account.models;

import prime.prime.domain.account.entity.AccountStatus;
import prime.prime.domain.role.Role;

public record AccountReturnWithPasswordDto(
    Long id,

    String email,

    String password,

    Role role,

    Long userId,

    AccountStatus status
) {

}
