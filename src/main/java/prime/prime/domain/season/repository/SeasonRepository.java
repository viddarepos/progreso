package prime.prime.domain.season.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import prime.prime.domain.season.entity.Season;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Long>, JpaSpecificationExecutor<Season> {
  @Query("SELECT DISTINCT season FROM Season season LEFT JOIN FETCH season.users users WHERE season.id = :id")
  Optional<Season> findByIdAndFetchUsers(Long id);

  @Query(value = "SELECT s FROM Season s LEFT JOIN FETCH s.users u WHERE u.id = :userId or s.owner.id = :userId",
          countQuery = "SELECT COUNT(DISTINCT s) FROM Season s LEFT JOIN s.users u WHERE u.id = :userId OR s.owner.id" +
                  " = :userId")
  Page<Season> findSeasonsByUserId(Pageable pageable, Long userId);

  boolean existsById(Long id);

  @Query("SELECT s.id FROM Season s")
  List<Long> getAllSeasonIds();

  @Query(value = "SELECT s.id FROM Season s LEFT JOIN s.users u WHERE u.id = :userId or s.owner.id = :userId")
  List<Long> getAllSeasonIdsForUser(Long userId);
}
