package prime.prime.domain.event.mappers.custom;

import org.springframework.stereotype.Component;
import prime.prime.domain.event.models.EventRequestDto;
import prime.prime.domain.event.models.EventUpdateDto;

import java.time.LocalDateTime;

@Component
public class EndTimeMapper {

    @EndTimeMapping
    public LocalDateTime calculateEndTime(EventRequestDto eventRequestDto) {
        return eventRequestDto.startTime().plusMinutes(eventRequestDto.duration());
    }

    @EndTimeMapping
    public LocalDateTime calculateEndTime(EventUpdateDto eventUpdateDto) {
        return eventUpdateDto.startTime().plusMinutes(eventUpdateDto.duration());
    }
}
