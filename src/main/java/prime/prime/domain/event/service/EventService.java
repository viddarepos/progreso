package prime.prime.domain.event.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import prime.prime.domain.event.models.*;
import prime.prime.domain.eventattendees.entity.EventAttendee;
import prime.prime.domain.season.entity.Season;
import prime.prime.infrastructure.security.ProgresoUserDetails;

public interface EventService {

    EventResponseWithAttendeesDto create(EventRequestDto eventRequestDto,
        ProgresoUserDetails userDetails);

    EventResponseWithAttendeesDto getById(Long id);

    Page<EventResponseWithAttendeesDto> getAll(SearchEventDto searchEventDto, Pageable pageable,
        ProgresoUserDetails progresoUserDetails);

    EventResponseWithAttendeesDto update(Long id, EventUpdateDto eventUpdateDto,
        ProgresoUserDetails progresoUserDetails);

    void delete(Long id);

    boolean checkIfEventAttendeesAreAssignedToSeason(Season season, Set<EventAttendee> attendees);

    List<CalendarEventResponseDto> getEventsByDate(LocalDate startDate, LocalDate endDate,
                                                   ProgresoUserDetails progresoUserDetails);
}
