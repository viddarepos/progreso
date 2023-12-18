package prime.prime.domain.user.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import prime.prime.domain.user.entity.User;
import prime.prime.domain.user.repository.Projection.UserProjection;


@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    boolean existsUserByAccountEmail(String email);

    Optional<UserProjection> findUserByAccountEmail(String email);

    Optional<User> findByAccountEmail(String email);
}
