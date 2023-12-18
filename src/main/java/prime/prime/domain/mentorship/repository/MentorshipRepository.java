package prime.prime.domain.mentorship.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import prime.prime.domain.mentorship.entity.Mentorship;

public interface MentorshipRepository extends JpaRepository<Mentorship, Long> {

}
