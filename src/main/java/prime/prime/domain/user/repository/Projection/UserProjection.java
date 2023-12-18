package prime.prime.domain.user.repository.Projection;

import prime.prime.domain.account.entity.AccountStatus;

public interface UserProjection {

    Long getId();

    Long getAccountId();

    String getAccountEmail();

    String getAccountPassword();

    String getAccountRole();

    AccountStatus getAccountStatus();

    void setId(Long id);

    void setAccountId(Long accountId);

    void setAccountEmail(String email);

    void setAccountPassword(String password);

    void setAccountRole(String role);

    void setAccountStatus(AccountStatus status);


}
