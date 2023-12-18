package prime.prime.domain.account.service;

import prime.prime.domain.account.entity.Account;
import prime.prime.domain.account.entity.AccountStatus;
import prime.prime.domain.account.models.AccountReturnWithPasswordDto;
import prime.prime.domain.user.repository.Projection.UserProjection;

public interface AccountService {

    AccountReturnWithPasswordDto returnAccountByEmail(String email);

    Account update(Long id, String password);

    void archive(Long id);

    void updateStatus(Long id, AccountStatus status);

    UserProjection checkAccountStatus(String email);

    Account findAccountByUserId(Long userId);
}
