package prime.prime.domain.event.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import prime.prime.domain.calendar.service.CalendarServiceImpl;
import prime.prime.domain.event.entity.Event;
import prime.prime.domain.event.mappers.EventMapper;
import prime.prime.domain.event.models.*;
import prime.prime.domain.event.repository.EventRepository;
import prime.prime.domain.event.repository.EventSpecification;
import prime.prime.domain.eventattendees.entity.EventAttendee;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.season.service.SeasonService;
import prime.prime.domain.season.utility.SeasonUtility;
import prime.prime.domain.user.entity.IntegrationType;
import prime.prime.domain.user.entity.User;
import prime.prime.domain.user.service.UserService;
import prime.prime.infrastructure.exception.EventException;
import prime.prime.infrastructure.exception.InvalidDateException;
import prime.prime.infrastructure.exception.MultipleAttendeesException;
import prime.prime.infrastructure.exception.NotFoundException;
import prime.prime.infrastructure.jobs.EmailSendingJob;
import prime.prime.infrastructure.security.ProgresoUserDetails;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserService userService;
    private final EmailSendingJob emailSendingJob;
    private final CalendarServiceImpl calendarServiceImpl;

    private final SeasonService seasonService;

    public EventServiceImpl(EventRepository eventRepository,
        EventMapper eventMapper,
        UserService userService,
        EmailSendingJob emailSendingJob,
        CalendarServiceImpl calendarServiceImpl, SeasonService seasonService) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.userService = userService;
        this.emailSendingJob = emailSendingJob;
        this.calendarServiceImpl = calendarServiceImpl;
        this.seasonService = seasonService;
    }

    private void addEventAttendees(Set<Long> newAttendees, Event event, boolean required) {
        for (Long attendeeId : newAttendees) {
            event.getEventAttendees()
                .add(new EventAttendee(required, event, userService.getEntityById(attendeeId)));
        }
    }

    @Override
    public EventResponseWithAttendeesDto create(EventRequestDto eventRequestDto,
        ProgresoUserDetails userDetails) {
        User creator = userService.getEntityById(userDetails.getUserId());

        var optionalAttendees = eventRequestDto.optionalAttendees();
        var requiredAttendees = eventRequestDto.requiredAttendees();

        checkForMultipleAttendees(optionalAttendees, requiredAttendees);

        Event saveEvent = eventMapper.dtoToEvent(eventRequestDto);
        saveEvent.setCreator(creator);
        saveEvent.setEventAttendees(new HashSet<>());

        Season season = seasonService.findActiveSeason(creator, eventRequestDto.seasonId());

        addEventAttendees(optionalAttendees, saveEvent, false);
        addEventAttendees(requiredAttendees, saveEvent, true);

        checkIfEventAttendeesAreAssignedToSeason(season, saveEvent.getEventAttendees());
        saveEvent.setSeason(season);

        isCreatorPresent(saveEvent);

        eventRepository.save(saveEvent);
        if (hasAttendeesWithIntegration(saveEvent.getEventAttendees(), IntegrationType.GOOGLE)) {
            saveGoogleEventIdOfCreatedEvent(saveEvent);
        }
        sendEmailToAttendees(saveEvent, creator, true);

        return eventMapper.eventToEventWithAttendeesDto(saveEvent);
    }

    private void saveGoogleEventIdOfCreatedEvent(Event createdEvent) {
        createdEvent.setGoogleCalendarEventId(calendarServiceImpl.createEvent(createdEvent));
        eventRepository.save(createdEvent);
    }

    @Override
    public EventResponseWithAttendeesDto getById(Long id) {
        return eventMapper.eventToEventWithAttendeesDto(eventRepository.findById(id)
            .orElseThrow(
                () -> new NotFoundException(Event.class.getSimpleName(), "id", id.toString())
            )
        );
    }

    @Override
    public Page<EventResponseWithAttendeesDto> getAll(SearchEventDto searchEventDto,
        Pageable pageable, ProgresoUserDetails progresoUserDetails) {
        if (searchEventDto.seasonId() == null && !progresoUserDetails.hasRole("ROLE_ADMIN")) {
            throw new EventException("Season id cannot be null");
        }

        User user = userService.getEntityById(progresoUserDetails.getUserId());
        if (user.isAdmin() && searchEventDto.seasonId() != null) {
            seasonService.existsById(searchEventDto.seasonId());
        }

        SeasonUtility.validateSeasons(user, searchEventDto.seasonId());

        return eventRepository
            .findAll(new EventSpecification(searchEventDto), pageable)
            .map(eventMapper::eventToEventWithAttendeesDto);
    }

    private void checkForMultipleAttendees(Set<Long> attendees, Set<Long> requiredAttendees) {
        boolean check = attendees.stream()
            .anyMatch(requiredAttendee -> requiredAttendees.stream()
                .anyMatch(a -> a.equals(requiredAttendee))
            );

        if (check) {
            throw new MultipleAttendeesException("Can not pass the same attendee in both lists!");
        }
    }

    @Override
    public EventResponseWithAttendeesDto update(Long id, EventUpdateDto eventUpdateDto,
        ProgresoUserDetails userDetails) {
        User editor = userService.getEntityById(userDetails.getUserId());

        Event foundEvent = eventRepository.findById(id)
            .orElseThrow(
                () -> new NotFoundException(Event.class.getSimpleName(), "id", id.toString())
            );

        if (!CollectionUtils.isEmpty(eventUpdateDto.optionalAttendees())
            && !CollectionUtils.isEmpty(eventUpdateDto.requiredAttendees())) {
            checkForMultipleAttendees(eventUpdateDto.optionalAttendees(),
                eventUpdateDto.requiredAttendees());
        }

        updateEvent(eventUpdateDto, foundEvent);

        eventRepository.save(foundEvent);

        updateGoogleEventOfUpdatedEvent(foundEvent);

        if (eventUpdateDto.notifyAttendees()) {
            sendEmailToAttendees(foundEvent, editor, false);
        }

        return eventMapper.eventToEventWithAttendeesDto(foundEvent);
    }

    @Override
    public void delete(Long id) {
        Event foundEvent = eventRepository.findById(id)
            .orElseThrow(
                () -> new NotFoundException(Event.class.getSimpleName(), "id", id.toString())
            );
        boolean hasGoogleCalenderEventId = hasGoogleCalenderEvent(foundEvent);
        eventRepository.delete(foundEvent);
        if (hasGoogleCalenderEventId) {
            calendarServiceImpl.deleteEvent(foundEvent);
        }
    }

    @Override
    public boolean checkIfEventAttendeesAreAssignedToSeason(Season season,
        Set<EventAttendee> attendees) {

        Set<EventAttendee> users = attendees.stream().filter(
                (attendee -> !attendee.isAdmin()
                    && userService.checkIfUserIsNotAssignedToSeason(attendee.getUser(), season)))
            .collect(Collectors.toSet());
        Set<Long> idUsers = users.stream().map(attendee -> attendee.getUser().getId())
            .collect(Collectors.toSet());

        if (!users.isEmpty()) {
            throw new EventException(
                "Event attendee's with id's: " + idUsers
                    + " need to be connected with the season"
                    + " with id " + season.getId());
        } else {
            return true;
        }
    }

    @Override
    public List<CalendarEventResponseDto> getEventsByDate(LocalDate startDate, LocalDate endDate,
                                                          ProgresoUserDetails progresoUserDetails) {

        LocalDateTime startDateTime = startDate.atTime(LocalTime.MIN);
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        if(startDate.isAfter(endDate)) {
            throw new InvalidDateException("Start Date cannot be after End Date");
        }

        List<Event> events = progresoUserDetails.hasRole("ROLE_ADMIN")
                ? eventRepository.findEventsByDate(startDateTime, endDateTime)
                : eventRepository.findEventsByDateForUser(startDateTime, endDateTime, progresoUserDetails.getUserId());

        return eventMapper.eventsToCalendarEventDtos(events);
    }

    private void updateEvent(EventUpdateDto eventUpdateDto, Event foundEvent) {

        var allAttendees = new HashSet<EventAttendee>();

        if (eventUpdateDto.optionalAttendees() != null) {
            eventUpdateDto.optionalAttendees().forEach(userId -> allAttendees.add(
                new EventAttendee(false, foundEvent, userService.getEntityById(userId))));
        }

        if (eventUpdateDto.requiredAttendees() != null) {
            eventUpdateDto.requiredAttendees().forEach(userId -> allAttendees.add(
                new EventAttendee(true, foundEvent, userService.getEntityById(userId))));
        }

        foundEvent.setEventAttendees(allAttendees);
        isCreatorPresent(foundEvent);
        eventMapper.updateEventFromDto(eventUpdateDto, foundEvent);
    }

    private void isCreatorPresent(Event foundEvent) {
        var isCreatorPresent = foundEvent.getEventAttendees().stream().anyMatch(
            eventAttendee -> eventAttendee.getUser().getId()
                .equals(foundEvent.getCreator().getId()));

        if (!isCreatorPresent) {
            foundEvent.getEventAttendees()
                .add(new EventAttendee(true, foundEvent, foundEvent.getCreator()));
        }
    }

    private void sendEmailToAttendees(Event event, User loggedUser, boolean isNewEvent) {
        String eventAction = isNewEvent ? "created" : "updated";
        String subject = "Event " + eventAction + ": " + event.getTitle();
        Map<String, String> templateData = new HashMap<>();

        for (EventAttendee attendee : event.getEventAttendees()) {
            if (attendee.getUser().getId().equals(loggedUser.getId())) {
                continue;
            }
            scheduleMailSending(attendee.getUser(), event, subject, eventAction, templateData);
        }
    }

    private void scheduleMailSending(User user, Event event, String emailSubject,
        String eventAction, Map<String, String> templateData) {
        templateData.put("fullName", user.getFullName());
        templateData.put("title", event.getTitle());
        templateData.put("description", event.getDescription());
        templateData.put("startTime",
            event.getStartTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        templateData.put("duration", event.getDuration().toMinutes() + " minutes");
        templateData.put("eventAction", eventAction);
        templateData.put("template", "event_notification");

        emailSendingJob.scheduleEmailJob(user.getAccount().getEmail(),emailSubject,templateData);
    }

    private boolean hasAttendeesWithIntegration(Set<EventAttendee> eventAttendees,
        IntegrationType integrationType) {
        return eventAttendees.stream()
            .anyMatch(eventAttendee -> eventAttendee.getUser().hasIntegration(integrationType));
    }

    private boolean hasGoogleCalenderEvent(Event event) {
        return event.getGoogleCalendarEventId() != null;
    }

    private void updateGoogleEventOfUpdatedEvent(Event event) {
        if (!hasGoogleCalenderEvent(event) && hasAttendeesWithIntegration(
            event.getEventAttendees(), IntegrationType.GOOGLE)) {
            saveGoogleEventIdOfCreatedEvent(event);
        }

        if (hasAttendeesWithIntegration(event.getEventAttendees(),
            IntegrationType.GOOGLE)) {
            calendarServiceImpl.updateEvent(event);
        }

        if (hasGoogleCalenderEvent(event) && !hasAttendeesWithIntegration(
            event.getEventAttendees(), IntegrationType.GOOGLE)) {
            calendarServiceImpl.deleteEvent(event);
            event.setGoogleCalendarEventId(null);
            eventRepository.save(event);
        }
    }
}