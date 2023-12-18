package prime.prime.domain.googleauthorization.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prime.prime.domain.googleauthorization.entity.GoogleAuthorization;

@Repository
public interface GoogleAuthorizationRepository extends JpaRepository<GoogleAuthorization, Long> {
}
