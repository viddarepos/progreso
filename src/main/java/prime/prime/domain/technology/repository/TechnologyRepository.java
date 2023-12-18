package prime.prime.domain.technology.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import prime.prime.domain.technology.entity.Technology;

import java.util.Optional;

@Repository
public interface TechnologyRepository extends JpaRepository<Technology,Long> {
    boolean existsByName(String name);
    Optional<Technology> findByName(String name);

}
