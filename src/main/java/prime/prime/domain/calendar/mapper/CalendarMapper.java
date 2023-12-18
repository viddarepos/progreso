package prime.prime.domain.calendar.mapper;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import prime.prime.domain.user.entity.IntegrationType;

@Mapper(componentModel = "spring")
public interface CalendarMapper {

    @Mapping(target = "id", source = "googleCalendarEventId")
    @Mapping(target = "summary", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "start", source = "startTime", qualifiedByName = "mapStartDateTime")
    @Mapping(target = "end", expression = "java(convertToGoogleCalendarEndDateTime(progresoEvent.getStartTime(), progresoEvent.getDuration()))")
    @Mapping(target = "attendees", source = "eventAttendees", qualifiedByName = "mapAttendees")
    Event progresoEventToGoogleEvent(prime.prime.domain.event.entity.Event progresoEvent);

    @Named("mapStartDateTime")
    default EventDateTime convertToGoogleCalendarStartDateTime(LocalDateTime startTime) {
        long calendarEventStartTime = startTime.atZone(ZoneId.systemDefault())
            .toInstant().toEpochMilli();
        DateTime startDateTime = new DateTime(calendarEventStartTime);
        return new EventDateTime().setDateTime(startDateTime)
            .setTimeZone(TimeZone.getDefault().getID());
    }

    @Named("mapEndDateTime")
    default EventDateTime convertToGoogleCalendarEndDateTime(LocalDateTime startTime,
        Duration duration) {
        LocalDateTime endTime = startTime.plus(duration);
        long calendarEventEndTime = endTime.atZone(ZoneId.systemDefault()).toInstant()
            .toEpochMilli();
        DateTime endDateTime = new DateTime(calendarEventEndTime);
        return new EventDateTime().setDateTime(endDateTime)
            .setTimeZone(TimeZone.getDefault().getID());
    }

    @Named("mapAttendees")
    default List<EventAttendee> mapAttendees(
        Set<prime.prime.domain.eventattendees.entity.EventAttendee> eventAttendees) {
        return eventAttendees.stream()
            .filter(eventAttendee -> eventAttendee.getUser().hasIntegration(IntegrationType.GOOGLE))
            .map(eventAttendee -> {
                if (eventAttendee.isRequired()) {
                    return new EventAttendee().setEmail(
                            eventAttendee.getUser().getAccount().getEmail())
                        .setResponseStatus("needsAction");
                } else {
                    return new EventAttendee().setEmail(
                            eventAttendee.getUser().getAccount().getEmail())
                        .setOptional(true)
                        .setResponseStatus("needsAction");
                }
            }).collect(Collectors.toList());
    }
}
