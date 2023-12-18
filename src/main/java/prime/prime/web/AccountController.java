package prime.prime.web;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import prime.prime.domain.account.entity.Account;
import prime.prime.domain.account.models.AccountReturnWithPasswordDto;
import prime.prime.domain.account.service.AccountService;

@RestController
@RequestMapping("/account")
@Hidden
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/email/{email}")
    ResponseEntity<AccountReturnWithPasswordDto> getAccountByEmail(@PathVariable String email) {
        return ResponseEntity.ok(accountService.returnAccountByEmail(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.findAccountByUserId(id));
    }
}
