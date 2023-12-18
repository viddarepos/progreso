package prime.prime.domain.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import prime.prime.domain.account.entity.Account;
import prime.prime.domain.account.entity.AccountStatus;
import prime.prime.domain.account.mapper.AccountMapper;
import prime.prime.domain.account.models.AccountReturnWithPasswordDto;
import prime.prime.domain.account.repository.AccountRepository;
import prime.prime.domain.role.Role;
import prime.prime.domain.user.repository.Projection.UserProjection;
import prime.prime.domain.user.repository.UserRepository;
import prime.prime.domain.user.service.UserServiceImpl;
import prime.prime.infrastructure.exception.ArchivedUserException;
import prime.prime.infrastructure.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AccountServiceImplTest {

    @InjectMocks
    private AccountServiceImpl accountService;
    @Mock
    AccountRepository accountRepository;
    @Mock
    UserServiceImpl userService;
    @Mock
    UserRepository userRepository;
    @Mock
    private AccountMapper accountMapper;
    private static Account account;
    private static UserProjection userProjection;
    private static AccountReturnWithPasswordDto accountReturnWithPasswordDto;
    private static final ProjectionFactory factory = new SpelAwareProxyProjectionFactory();

    @BeforeAll
    static void setUp() {

        account = new Account();
        account.setId(1L);
        account.setEmail("testing@gmail.com");
        account.setPassword("Testing_16");
        account.setRole(Role.ADMIN);
        account.setStatus(AccountStatus.INVITED);
    }

    @BeforeEach
    void setUserProjection() {
        userProjection = factory.createProjection(UserProjection.class);
        userProjection.setAccountId(account.getId());
        userProjection.setId(1L);
        userProjection.setAccountEmail(account.getEmail());
        userProjection.setAccountPassword(account.getPassword());
        userProjection.setAccountStatus(AccountStatus.ACTIVE);
        userProjection.setAccountRole("ROLE_ADMIN");

        accountReturnWithPasswordDto = new AccountReturnWithPasswordDto(1L, "testing@gmail.com",
            "Testing_16", Role.ADMIN, 1L, AccountStatus.ACTIVE);
    }

    @Test
    void archive_ValidIdAndNotArchivedAccount_Successful() {
        account.setStatus(AccountStatus.ACTIVE);
        when(accountRepository.findById(account.getId())).thenReturn(Optional.ofNullable(account));

        accountService.archive(account.getId());

        verify(accountRepository).save(account);
    }

    @Test
    void archive_InvalidId_ThrowsNotFoundException() {
        when(accountRepository.findById(account.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> accountService.archive(account.getId()));
    }

    @Test
    void archive_AccountAlreadyArchived_ThrowsArchivedUserException() {
        account.setStatus(AccountStatus.ARCHIVED);
        when(accountRepository.findById(account.getId())).thenReturn(Optional.ofNullable(account));

        assertThrows(ArchivedUserException.class, () -> accountService.archive(account.getId()));
    }

    @Test
    void updateStatus_Successful() {
        when(accountRepository.findById(account.getId())).thenReturn(Optional.ofNullable(account));

        accountService.updateStatus(account.getId(), AccountStatus.ACTIVE);

        verify(accountRepository).save(account);
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
    }

    @Test
    void checkAccountStatus_Active_Successful() {
        when(userRepository.findUserByAccountEmail("testing@gmail.com")).thenReturn(
            Optional.ofNullable(userProjection));
        when(userService.findUserByAccountEmail("testing@gmail.com")).thenReturn(userProjection);

        UserProjection returnedUser = accountService.checkAccountStatus(
            userProjection.getAccountEmail());

        assertEquals(AccountStatus.ACTIVE, returnedUser.getAccountStatus());
    }

    @Test
    void checkAccountStatus_Invited_Successful() {
        userProjection.setAccountStatus(AccountStatus.INVITED);
        when(userRepository.findUserByAccountEmail("testing@gmail.com")).thenReturn(
            Optional.ofNullable(userProjection));
        when(userService.findUserByAccountEmail("testing@gmail.com")).thenReturn(userProjection);
        when(accountRepository.findById(1L)).thenReturn(Optional.ofNullable(account));

        UserProjection returnedUser = accountService.checkAccountStatus(
            userProjection.getAccountEmail());

        verify(accountRepository).save(account);
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertEquals(AccountStatus.INVITED, returnedUser.getAccountStatus());
    }

    @Test
    void checkAccountStatus_Archived_ThrowsNotFoundException() {
        userProjection.setAccountStatus(AccountStatus.ARCHIVED);
        when(userService.findUserByAccountEmail("testing@gmail.com")).thenReturn(userProjection);

        assertThrows(NotFoundException.class,
            () -> accountService.checkAccountStatus(userProjection.getAccountEmail()));
    }

    @Test
    void returnAccountByEmail_ValidEmail_Successful() {
        when(userService.findUserByAccountEmail("testing@gmail.com")).thenReturn(userProjection);
        when(accountMapper.accountToReturnWithPasswordDto(userProjection)).thenReturn(
            accountReturnWithPasswordDto);

        AccountReturnWithPasswordDto returnedAccount = accountService.returnAccountByEmail(
            "testing@gmail.com");

        assertEquals("testing@gmail.com", returnedAccount.email());
        assertEquals("Testing_16", returnedAccount.password());
        assertEquals(Role.ADMIN, returnedAccount.role());
        assertEquals(AccountStatus.ACTIVE, returnedAccount.status());
    }

    @Test
    void update_ValidId_Successful() {
        Account accountWithNewPassword = new Account();
        accountWithNewPassword.setId(1L);
        accountWithNewPassword.setPassword("testing123@+");
        when(accountRepository.findById(1L)).thenReturn(Optional.ofNullable(account));
        when(accountRepository.save(account)).thenReturn(accountWithNewPassword);

        Account returnedAccount = accountService.update(1L, "testing123@+");

        assertEquals("testing123@+", returnedAccount.getPassword());
    }

    @Test
    void update_InvalidId_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class, () -> accountService.update(3L, "testing123@+"));
    }
}
