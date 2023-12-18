package prime.prime.domain.account.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import prime.prime.domain.account.entity.Account;
import prime.prime.domain.account.entity.AccountStatus;
import prime.prime.domain.account.mapper.AccountMapper;
import prime.prime.domain.account.models.AccountReturnWithPasswordDto;
import prime.prime.domain.account.repository.AccountRepository;
import prime.prime.domain.user.entity.User;
import prime.prime.domain.user.repository.Projection.UserProjection;
import prime.prime.domain.user.service.UserService;
import prime.prime.infrastructure.exception.ArchivedUserException;
import prime.prime.infrastructure.exception.NotFoundException;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final UserService userService;

    public AccountServiceImpl(AccountRepository accountRepository, AccountMapper accountMapper,
        @Lazy UserService userService) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.userService = userService;
    }

    @Override
    public AccountReturnWithPasswordDto returnAccountByEmail(String email) {
        UserProjection userProjection = checkAccountStatus(email);
        return accountMapper.accountToReturnWithPasswordDto(userProjection);
    }

    public Account update(Long id, String password) {
        Account account = accountRepository.findById(id)
            .orElseThrow(
                () -> new NotFoundException(Account.class.getSimpleName(), "id", id.toString()));

        accountMapper.update(password, account);
        return accountRepository.save(account);
    }

    @Override
    public void archive(Long id) {
        updateStatus(id, AccountStatus.ARCHIVED);
    }

    @Override
    public void updateStatus(Long id, AccountStatus status) {
        Account account = findById(id);
        if (account.getStatus().equals(AccountStatus.ARCHIVED) && status.equals(
            AccountStatus.ARCHIVED)) {
            throw new ArchivedUserException(Account.class.getSimpleName(), "id", id);
        }
        account.setStatus(status);
        accountRepository.save(account);
    }

    @Override
    public UserProjection checkAccountStatus(String email) {
        UserProjection userProjection = userService.findUserByAccountEmail(email);

        if (userProjection.getAccountStatus().equals(AccountStatus.INVITED)) {
            updateStatus(userProjection.getAccountId(), AccountStatus.ACTIVE);
            return userProjection;
        } else if (userProjection.getAccountStatus().equals(AccountStatus.ACTIVE)) {
            return userProjection;
        } else {
            throw new NotFoundException(Account.class.getSimpleName(), "email",
                userProjection.getAccountEmail());
        }
    }

    @Override
    public Account findAccountByUserId(Long userId) {
        User user = userService.findUserById(userId);
        return user.getAccount();
    }

    private Account findById(Long id) {
        return accountRepository.findById(id)
            .orElseThrow(
                () -> new NotFoundException(Account.class.getSimpleName(), "id", id.toString()));
    }
}
