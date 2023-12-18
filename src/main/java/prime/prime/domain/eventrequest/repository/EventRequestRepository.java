package prime.prime.domain.eventrequest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import prime.prime.domain.eventrequest.entity.EventRequest;

@Repository
public interface EventRequestRepository extends JpaRepository<EventRequest, Long>,
    JpaSpecificationExecutor<EventRequest> {

}
