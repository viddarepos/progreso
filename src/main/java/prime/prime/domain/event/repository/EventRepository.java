package prime.prime.domain.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import prime.prime.domain.event.entity.Event;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>,
        JpaSpecificationExecutor<Event> {

    @Query(value =
            "SELECT DISTINCT e.* FROM event e " +
                    "INNER JOIN events_attendees a " +
                    "ON e.id = a.event_id " +
                    "WHERE e.start_time BETWEEN :startDate AND :endDate " +
                    "OR (e.start_time + INTERVAL e.duration/1000000000 SECOND) BETWEEN :startDate AND :endDate " +
                    "AND a.user_id = :userId", nativeQuery = true)
    List<Event> findEventsByDateForUser(@Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate,
                               @Param("userId") Long userId);

    @Query(value = "SELECT * FROM event " +
            "WHERE start_time BETWEEN :startDate AND :endDate " +
            "OR (start_time + INTERVAL duration/1000000000 SECOND) BETWEEN :startDate AND :endDate",nativeQuery = true)
    List<Event> findEventsByDate(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);
}
