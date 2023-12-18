package prime.prime.domain.event.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import prime.prime.domain.account.entity.Account;
import prime.prime.domain.calendar.service.CalendarServiceImpl;
import prime.prime.domain.event.entity.Event;
import prime.prime.domain.event.mappers.EventMapper;
import prime.prime.domain.event.models.*;
import prime.prime.domain.event.repository.EventRepository;
import prime.prime.domain.event.repository.EventSpecification;
import prime.prime.domain.eventattendees.entity.EventAttendee;
import prime.prime.domain.role.Role;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.season.service.SeasonService;
import prime.prime.domain.user.entity.User;
import prime.prime.domain.user.service.UserService;
import prime.prime.infrastructure.email_sender.config.Email;
import prime.prime.infrastructure.email_sender.service.EmailService;
import prime.prime.infrastructure.exception.InvalidDateException;
import prime.prime.infrastructure.exception.MultipleAttendeesException;
import prime.prime.infrastructure.exception.NotFoundException;
import prime.prime.infrastructure.security.ProgresoUserDetails;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventMapper eventMapper;
    @Mock
    private UserService userService;
    @Mock
    private EmailService emailService;
    @Mock
    CalendarServiceImpl calendarService;

    @Mock
    SeasonService seasonService;


    @InjectMocks
    EventServiceImpl eventService;
    private static Event event;
    private static EventRequestDto eventRequestDto;
    private static EventUpdateDto eventUpdateDto;
    private static EventResponseWithAttendeesDto responseWithAttendeesDTO;
    private static User creator;
    private static Set<EventAttendee> attendees;
    private static CalendarEventResponseDto calendarEventResponseDto;

    @BeforeAll
    static void setUp() {
        Role role = Role.ADMIN;

        Account account = new Account();
        account.setId(1L);
        account.setEmail("test@mail.com");
        account.setPassword("1Password@");
        account.setRole(role);

        Set<Season> seasons = new HashSet<>();
        Season season = new Season();
        season.setStartDate(LocalDate.now().minusDays(1));
        season.setEndDate(LocalDate.now().plusMonths(6));
        season.setId(1L);
        seasons.add(season);

        creator = new User();
        creator.setId(1L);
        creator.setFullName("Test");
        creator.setAccount(account);
        creator.setSeasons(seasons);

        event = new Event();
        event.setTitle("Title");
        event.setDescription("Description");
        event.setStartTime(LocalDateTime.of(LocalDate.of(2022, 7, 7), LocalTime.of(21, 30)));
        event.setDuration(Duration.ofMinutes(30));
        event.setCreator(creator);
        event.setSeason(season);

        EventAttendee eventAttendee = new EventAttendee();

        eventAttendee.setId(1L);
        eventAttendee.setEvent(event);
        eventAttendee.setRequired(false);

        eventRequestDto = new EventRequestDto(
            "Title", "Description",
            LocalDateTime.of(LocalDate.of(2022, 7, 7), LocalTime.of(21, 30)),
            30L, 1L, Set.of(1L), Set.of(2L));

        eventUpdateDto = new EventUpdateDto("Title", "Description",
            LocalDateTime.of(LocalDate.of(2022, 7, 7), LocalTime.of(21, 30)),
            30L, true, Set.of(1L), Set.of(2L));

        responseWithAttendeesDTO = new EventResponseWithAttendeesDto(1L,
            "Title", "Description",
            LocalDateTime.of(LocalDate.of(2022, 7, 7), LocalTime.of(21, 30)),
            30L, LocalDateTime.of(2022, 7, 7, 22, 0), 1L, 1L, Set.of(new AttendeesDto(creator.getId(), creator.getFullName(),
            creator.getAccount().getEmail(), false)));

        calendarEventResponseDto = new CalendarEventResponseDto(1L,
                "Title", LocalDateTime.of(2022, 7, 7, 21, 30), 30L, LocalDateTime.of(2022, 7, 7, 22, 0));
    }

    @Test
    void create_ValidEventRequestDto_Successful() {
        Email email = new Email("email@hotmail.com", "Subject", new HashMap<>());
        ProgresoUserDetails userDetails = new ProgresoUserDetails(creator.getAccount().getId(),
            creator.getId(), creator.getAccount().getEmail(),
            creator.getAccount().getPassword(),
            Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + creator.getAccount().getRole().name())));
        event.setEventAttendees(attendees);
        emailService.send(email);

        when(eventMapper.dtoToEvent(eventRequestDto)).thenReturn(event);
        when(eventMapper.eventToEventWithAttendeesDto(any(Event.class))).thenReturn(
            responseWithAttendeesDTO);
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(calendarService.createEvent(event)).thenReturn(event.getGoogleCalendarEventId());
        when(userService.getEntityById(anyLong())).thenReturn(creator);
        when(seasonService.findById(eventRequestDto.seasonId())).thenReturn(event.getSeason());
        EventResponseWithAttendeesDto returnedData = eventService.create(eventRequestDto,
            userDetails);

        assertThat(returnedData).usingRecursiveComparison().isEqualTo(responseWithAttendeesDTO);
        verify(emailService).send(email);
    }

    @Test
    void getById_ValidId_Successful() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.ofNullable(event));
        when(eventMapper.eventToEventWithAttendeesDto(any(Event.class))).thenReturn(
            responseWithAttendeesDTO);

        EventResponseWithAttendeesDto returnedData = eventService.getById(1L);

        assertThat(returnedData).usingRecursiveComparison().isEqualTo(responseWithAttendeesDTO);
    }

    @Test
    void getById_InvalidId_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class, () -> eventService.getById(10L));
    }

    @Test
    void getAll_Valid_Successful() {
        List<Event> list = List.of(event);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> eventPage = new PageImpl<>(list, pageable, list.size());

        SearchEventDto searchEventDto = new SearchEventDto(1L);
        ProgresoUserDetails userDetails = new ProgresoUserDetails(creator.getAccount().getId(),
            creator.getId(),
            creator.getAccount().getEmail(), creator.getAccount().getPassword(),
            Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + creator.getAccount().getRole().name())));

        when(eventMapper.eventToEventWithAttendeesDto(event)).thenReturn(responseWithAttendeesDTO);

        when(
            eventRepository.findAll(any(EventSpecification.class), any(Pageable.class))).thenReturn(
            eventPage);
        when(userService.getEntityById(userDetails.getUserId())).thenReturn(creator);
        Page<EventResponseWithAttendeesDto> returnPage = eventService
            .getAll(searchEventDto, pageable, userDetails);

        assertEquals(1, returnPage.getTotalPages());
        assertEquals(1, returnPage.getTotalElements());
        assertNotNull(returnPage.getContent());
        assertThat(returnPage.getContent().get(0).attendees()).isNotNull();
    }

    @Test
    void getAll_EmptyPage_Successful() {
        List<Event> list = Collections.emptyList();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> eventPage = new PageImpl<>(list, pageable, 0);

        SearchEventDto searchEventDto = new SearchEventDto(1L);
        creator.getAccount().setRole(Role.MENTOR);
        ProgresoUserDetails userDetails = new ProgresoUserDetails(creator.getAccount().getId(),
            creator.getId(),
            creator.getAccount().getEmail(), creator.getAccount().getPassword(),
            Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + creator.getAccount().getRole().name())));

        when(
            eventRepository.findAll(any(EventSpecification.class), any(Pageable.class))).thenReturn(
            eventPage);
        when(userService.getEntityById(userDetails.getUserId())).thenReturn(creator);
        Page<EventResponseWithAttendeesDto> returnPage = eventService.getAll(searchEventDto,
            pageable, userDetails);

        assertEquals(0, returnPage.getTotalPages());
        assertEquals(0, returnPage.getTotalElements());
        assertNotNull(returnPage.getContent());
    }

    @Test
    void update_ValidId_Successful() {
        Email email = new Email("email@hotmail.com", "Subject", new HashMap<>());
        ProgresoUserDetails userDetails = new ProgresoUserDetails(creator.getAccount().getId(),
            creator.getId(), creator.getAccount().getEmail(),
            creator.getAccount().getPassword(),
            Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + creator.getAccount().getRole().name())));
        EventResponseWithAttendeesDto updateDTO = new EventResponseWithAttendeesDto(1L,
            "Updated Title", "Updated Description",
            LocalDateTime.of(LocalDate.of(2022, 9, 9), LocalTime.of(22, 40)),
            30L, LocalDateTime.of(2022, 7, 7, 22, 0), 1L, 1L,
            Set.of(new AttendeesDto(creator.getId(), creator.getFullName(),
                creator.getAccount().getEmail(), true)));

        event = new Event();
        event.setTitle("Updated Title");
        event.setDescription("Updated Description");
        event.setStartTime(LocalDateTime.of(LocalDate.of(2022, 9, 9), LocalTime.of(22, 40)));
        event.setDuration(Duration.ofMinutes(30));
        event.setCreator(creator);
        event.setEventAttendees(attendees);
        emailService.send(email);

        when(eventRepository.findById(anyLong())).thenReturn(Optional.ofNullable(event));
        when(eventMapper.eventToEventWithAttendeesDto(any(Event.class))).thenReturn(updateDTO);
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(calendarService.updateEvent(event)).thenReturn(event.getGoogleCalendarEventId());
        when(userService.getAllById(eventUpdateDto.optionalAttendees())).thenReturn(Set.of(creator));
        when(userService.getEntityById(anyLong())).thenReturn(creator);

        EventResponseWithAttendeesDto returnedData = eventService.update(1L, eventUpdateDto,
            userDetails);

        assertThat(returnedData).usingRecursiveComparison().isEqualTo(updateDTO);
        verify(emailService).send(email);
    }

    @Test
    void update_InvalidId_ThrowsNotFoundException() {
        ProgresoUserDetails userDetails = new ProgresoUserDetails(creator.getAccount().getId(),
            creator.getId(), creator.getAccount().getEmail(),
            creator.getAccount().getPassword(),
            Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + creator.getAccount().getRole().name())));
        EventUpdateDto updateDTO = new EventUpdateDto("Updated Title", "Updated Description",
            LocalDateTime.of(LocalDate.of(2022, 9, 9), LocalTime.of(22, 40)),
            30L, true, Set.of(1L), Set.of(1L));

        when(eventRepository.findById(userDetails.getUserId())).thenThrow(
            MultipleAttendeesException.class);

        assertThrows(MultipleAttendeesException.class,
            () -> eventService.update(1L, updateDTO, userDetails));
    }

    @Test
    void update_InvalidAttendeeId_ThrowsNotFoundException() {
        ProgresoUserDetails userDetails = new ProgresoUserDetails(creator.getAccount().getId(),
            creator.getId(), creator.getAccount().getEmail(),
            creator.getAccount().getPassword(),
            Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + creator.getAccount().getRole().name())));
        EventUpdateDto updateDTO = new EventUpdateDto("Updated Title", "Updated Description",
            LocalDateTime.of(LocalDate.of(2022, 9, 9), LocalTime.of(22, 40)),
            30L, true, Set.of(0L), Set.of(0L));

        when(eventRepository.findById(anyLong())).thenThrow(MultipleAttendeesException.class);

        assertThrows(MultipleAttendeesException.class,
            () -> eventService.update(1L, updateDTO, userDetails));
    }

    @Test
    void delete_ValidId_Successful() {
        when(eventRepository.findById(anyLong())).thenReturn(Optional.ofNullable(event));
        doNothing().when(calendarService).deleteEvent(event);

        eventService.delete(1L);

        verify(eventRepository, times(1)).delete(any(Event.class));
    }

    @Test
    void delete_InvalidId_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class, () -> eventService.delete(10L));
    }

    @Test
    public void get_EventsByDateForAdmin_Successful() {

        LocalDateTime startDateTime = LocalDateTime.of(2022, 7, 4, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2022, 8, 1, 23, 59, 59, 999_999_999);

        List<Event> events = new ArrayList<>();
        events.add(event);

        when(eventRepository.findEventsByDate(startDateTime, endDateTime)).thenReturn(events);

        List<CalendarEventResponseDto> responseDtos = new ArrayList<>();
        responseDtos.add(calendarEventResponseDto);

        when(eventMapper.eventsToCalendarEventDtos(events)).thenReturn(responseDtos);

        LocalDate startDate = LocalDate.of(2022, 7, 4);
        LocalDate endDate = LocalDate.of(2022, 8, 1);

        ProgresoUserDetails userDetails = new ProgresoUserDetails(
                1L, 1L, "test@mail.com", "1Password@",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        List<CalendarEventResponseDto> result = eventService.getEventsByDate(startDate, endDate, userDetails);

        assertEquals(result, responseDtos);
    }

    @Test
    public void get_EventsByDate_ThrowsInvalidDateException() {

        LocalDateTime startDateTime = LocalDateTime.of(2023, 7, 4, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2022, 8, 1, 23, 59, 59, 999_999_999);

        List<Event> events = new ArrayList<>();
        events.add(event);

        when(eventRepository.findEventsByDate(startDateTime, endDateTime)).thenReturn(events);

        LocalDate startDate = LocalDate.of(2023, 7, 4);
        LocalDate endDate = LocalDate.of(2022, 8, 1);

        ProgresoUserDetails userDetails = new ProgresoUserDetails(
                1L, 1L, "test@mail.com", "1Password@",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        assertThrows(InvalidDateException.class, ()-> eventService.getEventsByDate(startDate, endDate, userDetails));
    }
}