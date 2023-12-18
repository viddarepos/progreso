package prime.prime.domain.event.mappers;

import org.mapstruct.*;
import prime.prime.domain.event.entity.Event;
import prime.prime.domain.event.mappers.custom.EndTimeMapper;
import prime.prime.domain.event.mappers.custom.EndTimeMapping;
import prime.prime.domain.event.mappers.custom.EventDurationMapper;
import prime.prime.domain.event.mappers.custom.EventDurationMapping;
import prime.prime.domain.event.models.*;
import prime.prime.domain.season.service.SeasonService;
import prime.prime.domain.user.mappers.UserMapper;
import prime.prime.domain.user.mappers.custom.UserIdToUserEntityMapper;
import prime.prime.domain.user.mappers.custom.UserMapping;
import prime.prime.domain.user.service.UserService;

import java.util.List;


@Mapper(
        componentModel = "spring",
        uses = {UserService.class,
                UserMapper.class,
                EventDurationMapper.class,
                EndTimeMapper.class,
                UserIdToUserEntityMapper.class,
                SeasonService.class
        }
)
public interface EventMapper {

    @Mapping(target = "duration", qualifiedBy = EventDurationMapping.class)
    @Mapping(source = "creator.id", target = "creatorId")
    EventResponseDto eventToDto(Event event);

    @Mapping(target = "duration", qualifiedBy = EventDurationMapping.class)
    @Mapping(source = "creator.id", target = "creatorId")
    @Mapping(source = "eventAttendees", target = "attendees", qualifiedBy = UserMapping.class)
    @Mapping(source = "season.id", target = "seasonId")
    EventResponseWithAttendeesDto eventToEventWithAttendeesDto(Event event);

    @Mapping(target = "duration", qualifiedBy = EventDurationMapping.class)
    @Mapping(target = "endTime", source = "eventRequestDto", qualifiedBy = EndTimeMapping.class)
    Event dtoToEvent(EventRequestDto eventRequestDto);

    @Mapping(target = "duration", qualifiedBy = EventDurationMapping.class)
    CalendarEventResponseDto eventToCalendarEventDto(Event event);

    List<CalendarEventResponseDto> eventsToCalendarEventDtos(List<Event> events);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "duration", qualifiedBy = EventDurationMapping.class)
    @Mapping(target = "eventAttendees", ignore = true)
    @Mapping(target = "endTime", source = "dto", qualifiedBy = EndTimeMapping.class)
    void updateEventFromDto(EventUpdateDto dto, @MappingTarget Event event);
}
