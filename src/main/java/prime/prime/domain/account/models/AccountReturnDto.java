package prime.prime.domain.account.models;

import prime.prime.domain.account.entity.AccountStatus;
import prime.prime.domain.role.Role;

public record AccountReturnDto(
    Long id,

    String email,

    Role role,

    AccountStatus status
) {

}
