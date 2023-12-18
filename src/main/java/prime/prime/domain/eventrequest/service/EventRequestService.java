package prime.prime.domain.eventrequest.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import prime.prime.domain.eventrequest.models.*;
import prime.prime.infrastructure.security.ProgresoUserDetails;

public interface EventRequestService {

    EventRequestReturnDto create(EventRequestCreateDto eventRequestDto, ProgresoUserDetails user);

    Page<EventRequestReturnDto> getAll(SearchEventRequestDto searchEventRequestDto,
        Pageable pageable, ProgresoUserDetails userDetails);

    EventRequestReturnDto getById(Long id);

    EventRequestReturnDto update(Long id, EventRequestUpdateDto eventRequestUpdateDto);

    EventRequestReturnDto changeStatus(Long id, EventRequestStatusDto eventRequestStatusDTO);

    void deleteEventRequest(Long id);
}
