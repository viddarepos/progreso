package prime.prime.domain.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prime.prime.domain.account.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Account findByEmail(String toEmail);
}
