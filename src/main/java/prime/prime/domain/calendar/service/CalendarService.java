package prime.prime.domain.calendar.service;

import prime.prime.domain.event.entity.Event;

public interface CalendarService {
    String createEvent(Event event);
    String updateEvent(Event event);
    void deleteEvent(Event event);


}
