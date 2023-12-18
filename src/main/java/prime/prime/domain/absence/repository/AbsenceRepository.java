package prime.prime.domain.absence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import prime.prime.domain.absence.entity.AbsenceRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface AbsenceRepository extends JpaRepository<AbsenceRequest,Long>, JpaSpecificationExecutor<AbsenceRequest> {
    @Query(value = "SELECT COUNT(a) FROM AbsenceRequest a " +
            "WHERE a.requester.id = :userId AND a.season.id = :seasonId " +
            "AND ((a.startTime >= :requestedStartTime AND a.startTime <= :requestedEndTime) " +
            "OR (a.endTime >= :requestedStartTime AND a.endTime <= :requestedEndTime) " +
            "OR (a.startTime <= :requestedStartTime AND a.endTime >= :requestedEndTime)" +
            "OR (a.startTime >= :requestedStartTime AND a.endTime <= :requestedEndTime))")
    Long numberOfAbsencesForGivenDatesAndEmployee(Long userId, Long seasonId, LocalDateTime requestedStartTime, LocalDateTime requestedEndTime);

    @Query("SELECT a.id FROM AbsenceRequest a " +
            "WHERE a.startTime BETWEEN :startTime AND :endTime OR " +
            "a.endTime BETWEEN :startTime AND :endTime " +
            "AND a.season.id IN :seasonIds " +
            "AND a.status <> 'REJECTED'")
    List<Long> getAbsenceIdsByDateForUser(LocalDateTime startTime, LocalDateTime endTime, List<Long> seasonIds);
}
