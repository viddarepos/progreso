package prime.prime.domain.eventrequest.service;


import org.junit.jupiter.api.Assertions;
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
import prime.prime.domain.eventrequest.entity.EventRequest;
import prime.prime.domain.eventrequest.entity.EventRequestStatus;
import prime.prime.domain.eventrequest.mapper.EventRequestMapper;
import prime.prime.domain.eventrequest.models.*;
import prime.prime.domain.eventrequest.repository.EventRequestRepository;
import prime.prime.domain.eventrequest.repository.EventRequestSpecification;
import prime.prime.domain.role.Role;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.season.service.SeasonService;
import prime.prime.domain.user.entity.User;
import prime.prime.domain.user.service.UserService;
import prime.prime.infrastructure.email_sender.config.Email;
import prime.prime.infrastructure.exception.EventRequestException;
import prime.prime.infrastructure.exception.NotFoundException;
import prime.prime.infrastructure.exception.SeasonException;
import prime.prime.infrastructure.jobs.EmailSendingJob;
import prime.prime.infrastructure.security.ProgresoUserDetails;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventRequestServiceImplTest {

    @Mock
    private EventRequestRepository repository;
    @Mock
    private EventRequestMapper mapper;
    @Mock
    private UserService userService;
    @Mock
    private EmailSendingJob emailSendingJob;
    @Mock
    private SeasonService seasonService;
    @InjectMocks
    private EventRequestServiceImpl eventRequestService;
    private static EventRequest eventRequest;
    private static EventRequestCreateDto eventRequestCreateDto;
    private static EventRequestReturnDto eventRequestReturnDto;
    private static Account account;
    private static User intern;
    private static User mentor;
    private static User admin;
    private static ProgresoUserDetails adminDetails;
    private static Set<Season> seasons;
    private static Season season;

    @BeforeAll
    static void setUp() {
        Role role = Role.INTERN;

        account = new Account();
        account.setId(1L);
        account.setEmail("test@mail.com");
        account.setPassword("1Password@");
        account.setRole(role);

        Account mentorAccount = new Account();
        mentorAccount.setId(2L);
        mentorAccount.setEmail("testMentor@mail.com");
        mentorAccount.setPassword("1Password@");
        mentorAccount.setRole(Role.MENTOR);

        seasons = new HashSet<>();
        season = new Season();
        season.setStartDate(LocalDate.now().minusDays(1));
        season.setEndDate(LocalDate.now().plusMonths(6));
        seasons.add(season);

        intern = new User();
        intern.setId(1L);
        intern.setFullName("Cool Name");
        intern.setLocation("Cool Location");
        intern.setAccount(account);
        intern.setSeasons(null);

        mentor = new User();
        mentor.setId(1L);
        mentor.setFullName("Mentor");
        mentor.setLocation("Mentor location");
        mentor.setAccount(mentorAccount);

        Account adminAccount = new Account();
        adminAccount.setId(3L);
        adminAccount.setEmail("testAdmin@mail.com");
        adminAccount.setPassword("1Password@");
        adminAccount.setRole(Role.ADMIN);

        admin = new User();
        admin.setId(3L);
        admin.setFullName("Admin");
        admin.setLocation("Admin location");
        admin.setAccount(adminAccount);

        adminDetails = new ProgresoUserDetails(adminAccount.getId(), admin.getId(),
            adminAccount.getEmail(), adminAccount.getPassword(),
            Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + adminAccount.getRole().name())));

        eventRequest = new EventRequest();
        eventRequest.setId(1L);
        eventRequest.setTitle("Cool");
        eventRequest.setDescription("Cool");
        eventRequest.setStatus(EventRequestStatus.REQUESTED);
        eventRequest.setRequester(intern);
        eventRequest.setSeason(season);

        eventRequestCreateDto = new EventRequestCreateDto("Cool", "Cool", 1L);
        eventRequestReturnDto = new EventRequestReturnDto(
            eventRequest.getId(),
            eventRequest.getTitle(),
            eventRequest.getDescription(),
            eventRequest.getStatus().name(),
            eventRequest.getRequester().getId(),
            eventRequest.getSeason().getId()
        );
    }

    @Test
    void create_ValidEventRequestCreateDtoIntern_Successful() {
        intern.setSeasons(seasons);
        ProgresoUserDetails userDetails = new ProgresoUserDetails(account.getId(), intern.getId(),
            account.getEmail(), account.getPassword(),
            Collections.singleton(new SimpleGrantedAuthority("ROLE_" + account.getRole().name())));

        when(mapper.toEventRequest(eventRequestCreateDto)).thenReturn(eventRequest);
        when(mapper.fromEventRequest(eventRequest)).thenReturn(eventRequestReturnDto);
        when(repository.save(eventRequest)).thenReturn(eventRequest);
        when(userService.getEntityById(anyLong())).thenReturn(intern);

        EventRequestReturnDto actual = eventRequestService.create(eventRequestCreateDto,
            userDetails);

        assertThat(actual).usingRecursiveComparison().isEqualTo(eventRequestReturnDto);
    }

    @Test
    void create_ValidEventRequestCreateDtoAdmin_Successful() {
        ProgresoUserDetails userDetails = new ProgresoUserDetails(3L, 3L,
            "testAdmin@mail.com", "testAdmin@mail.com",
            Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));

        when(mapper.toEventRequest(eventRequestCreateDto)).thenReturn(eventRequest);
        when(mapper.fromEventRequest(eventRequest)).thenReturn(eventRequestReturnDto);
        when(repository.save(eventRequest)).thenReturn(eventRequest);
        when(userService.getEntityById(anyLong())).thenReturn(admin);
        when(seasonService.findById(1L)).thenReturn(season);

        EventRequestReturnDto actual = eventRequestService.create(eventRequestCreateDto,
            userDetails);

        assertThat(actual).usingRecursiveComparison().isEqualTo(eventRequestReturnDto);
    }

    @Test
    void create_CreatorWithoutSeason_ThrowsSeasonException() {
        intern.setSeasons(null);
        ProgresoUserDetails userDetails = new ProgresoUserDetails(account.getId(), intern.getId(),
            account.getEmail(), account.getPassword(),
            Collections.singleton(new SimpleGrantedAuthority("ROLE_" + account.getRole().name())));

        when(mapper.toEventRequest(eventRequestCreateDto)).thenReturn(eventRequest);
        when(mapper.fromEventRequest(eventRequest)).thenReturn(eventRequestReturnDto);
        when(repository.save(eventRequest)).thenReturn(eventRequest);
        when(userService.getEntityById(anyLong())).thenReturn(intern);
        when(seasonService.findActiveSeason(intern, 1L)).thenThrow(
            SeasonException.class);

        assertThrows(SeasonException.class, () -> eventRequestService.create(eventRequestCreateDto,
            userDetails));
    }

    @Test
    void getById_ValidId_Successful() {
        when(repository.findById(anyLong())).thenReturn(Optional.ofNullable(eventRequest));
        when(mapper.fromEventRequest(eventRequest)).thenReturn(eventRequestReturnDto);

        EventRequestReturnDto actual = eventRequestService.getById(1L);

        assertThat(actual).usingRecursiveComparison().isEqualTo(eventRequestReturnDto);
    }

    @Test
    void getById_InvalidId_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class, () -> eventRequestService.getById(0L));
    }

    @Test
    void getAll_StatusIsNotNull_Successful() {
        EventRequest eventRequest2 = new EventRequest();
        eventRequest2.setId(2L);
        eventRequest2.setTitle("Cool1");
        eventRequest2.setDescription("Cool1");
        eventRequest2.setStatus(EventRequestStatus.REQUESTED);
        eventRequest2.setRequester(intern);

        EventRequestReturnDto eventRequestReturnDto2 = new EventRequestReturnDto(
            eventRequest2.getId(),
            eventRequest2.getTitle(),
            eventRequest2.getDescription(),
            eventRequest2.getStatus().name(),
            eventRequest2.getRequester().getId(),
            null,
            null);

        SearchEventRequestDto searchEventRequestDto = new SearchEventRequestDto("REQUESTED", null);

        when(repository.findAll(any(EventRequestSpecification.class),
            any(Pageable.class))).thenReturn(new PageImpl<>(List.of(eventRequest, eventRequest2)));
        when(mapper.fromEventRequest(eventRequest)).thenReturn(eventRequestReturnDto);
        when(mapper.fromEventRequest(eventRequest2)).thenReturn(eventRequestReturnDto2);
        when(userService.getEntityById(adminDetails.getUserId())).thenReturn(admin);

        Page<EventRequestReturnDto> eventRequests = eventRequestService.getAll(
            searchEventRequestDto, Pageable.ofSize(2), adminDetails);

        assertThat(eventRequests.getContent().get(0)).usingRecursiveComparison()
            .isEqualTo(eventRequestReturnDto);
        assertThat(eventRequests.getContent().get(1)).usingRecursiveComparison()
            .isEqualTo(eventRequestReturnDto2);
    }

    @Test
    void getAll_StatusIsNull_Successful() {
        SearchEventRequestDto searchEventRequestDto = new SearchEventRequestDto(null, null);

        when(repository.findAll(any(EventRequestSpecification.class),
            any(Pageable.class))).thenReturn(new PageImpl<>(List.of(eventRequest)));
        when(mapper.fromEventRequest(eventRequest)).thenReturn(eventRequestReturnDto);
        when(userService.getEntityById(adminDetails.getUserId())).thenReturn(admin);

        Page<EventRequestReturnDto> eventRequests = eventRequestService.getAll(
            searchEventRequestDto, Pageable.ofSize(2), adminDetails);

        assertThat(eventRequests.getContent().get(0)).usingRecursiveComparison()
            .isEqualTo(eventRequestReturnDto);
    }

    @Test
    void getAll_SearchEventRequestDtoIsNull_ThrowsEventRequestException() {
        Assertions.assertThrows(EventRequestException.class,
            () -> eventRequestService.getAll(null, Pageable.ofSize(2), adminDetails));
    }

    @Test
    void getAll_EmptyPage_Successful() {
        when(repository.findAll(any(EventRequestSpecification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));
        when(userService.getEntityById(adminDetails.getUserId())).thenReturn(admin);

        SearchEventRequestDto searchEventRequestDto = new SearchEventRequestDto("APPROVED", null);

        Page<EventRequestReturnDto> eventRequests = eventRequestService.getAll(
            searchEventRequestDto, PageRequest.of(0, 10), adminDetails);

        assertEquals(0, eventRequests.getTotalPages());
        assertEquals(0, eventRequests.getTotalElements());
        assertNotNull(eventRequests.getContent());
    }

    @Test
    void update_ValidId_Success() {
        EventRequestUpdateDto updateDTO = new EventRequestUpdateDto("Update", "Update");
        EventRequestReturnDto responseDto =
            new EventRequestReturnDto(1L, "Update", "Update", "REQUESTED", 1L, 1L);

        when(repository.findById(anyLong())).thenReturn(Optional.ofNullable(eventRequest));
        when(mapper.fromEventRequest(eventRequest)).thenReturn(responseDto);
        when(repository.save(any(EventRequest.class))).thenReturn(eventRequest);

        EventRequestReturnDto update = eventRequestService.update(1L, updateDTO);

        assertThat(update).usingRecursiveComparison().isEqualTo(responseDto);
    }

    @Test
    void update_InvalidId_IdNotFound() {
        EventRequestUpdateDto updateDTO = new EventRequestUpdateDto("Update", "Update");

        assertThrows(NotFoundException.class,
            () -> eventRequestService.update(0L, updateDTO));
    }

    @Test
    void delete_ValidId_Successful() {
        when(repository.existsById(anyLong())).thenReturn(true);

        eventRequestService.deleteEventRequest(1L);

        verify(repository).deleteById(eventRequest.getId());
    }

    @Test
    void delete_InvalidId_IdNotFound() {
        assertThrows(NotFoundException.class,
            () -> eventRequestService.deleteEventRequest(0L));
    }

    @Test
    void changeStatus_Reject_Successful() {
        eventRequest.setStatus(EventRequestStatus.REQUESTED);
        EventRequestStatusDto rejectDto = new EventRequestStatusDto("Canceled",
            EventRequestStatus.REJECTED, null);
        EventRequestReturnDto responseDto =
            new EventRequestReturnDto(1L, "Reject", "Reject", "REJECTED", 1L, 1L);


        when(repository.findById(anyLong())).thenReturn(Optional.ofNullable(eventRequest));
        when(mapper.fromEventRequest(eventRequest)).thenReturn(responseDto);
        when(repository.save(any(EventRequest.class))).thenReturn(eventRequest);

        EventRequestReturnDto returnDto = eventRequestService.changeStatus(1L, rejectDto);
        emailSendingJob.scheduleEmailJob(eventRequest.getRequester().getAccount().getEmail(), "Test", Map.of(
                "fullName", "Test", "Test", "Test"));

        assertThat(returnDto).usingRecursiveComparison().isEqualTo(responseDto);
    }

    @Test
    void changeStatus_Schedule_Successful() {
        eventRequest.setStatus(EventRequestStatus.APPROVED);
        EventRequestStatusDto scheduleDTO = new EventRequestStatusDto("",
            EventRequestStatus.SCHEDULED, null);
        EventRequestReturnDto responseDto =
            new EventRequestReturnDto(1L, "Scheduled", "Scheduled", "SCHEDULED", 1L, 1L);
        Email email = new Email(eventRequest.getRequester().getAccount().getEmail(), "Scheduled",
            Map.of("fullName", "Test", "Test", ""));

        when(repository.findById(anyLong())).thenReturn(Optional.ofNullable(eventRequest));
        when(mapper.fromEventRequest(eventRequest)).thenReturn(responseDto);
        when(repository.save(any(EventRequest.class))).thenReturn(eventRequest);

        EventRequestReturnDto returnDto = eventRequestService.changeStatus(1L, scheduleDTO);
        emailSendingJob.scheduleEmailJob(eventRequest.getRequester().getAccount().getEmail(), "Test", Map.of(
                "fullName", "Test", "Test", "Test"));

        assertThat(returnDto).usingRecursiveComparison().isEqualTo(responseDto);
    }

    @Test
    void changeStatus_RejectAlreadyRejected_ThrowsEventRequestException() {
        EventRequestStatusDto rejectDto = new EventRequestStatusDto("Reject",
            EventRequestStatus.REJECTED, null);
        eventRequest.setStatus(EventRequestStatus.REJECTED);
        when(repository.findById(anyLong())).thenReturn(Optional.of(eventRequest));
        assertThrows(EventRequestException.class,
            () -> eventRequestService.changeStatus(eventRequest.getId(), rejectDto));
    }

    @Test
    void changeStatus_ScheduleAlreadyScheduled_ThrowsEventRequestException() {
        EventRequestStatusDto scheduleDto = new EventRequestStatusDto("Schedule",
            EventRequestStatus.SCHEDULED, null);
        eventRequest.setStatus(EventRequestStatus.SCHEDULED);
        when(repository.findById(anyLong())).thenReturn(Optional.of(eventRequest));
        assertThrows(EventRequestException.class,
            () -> eventRequestService.changeStatus(eventRequest.getId(), scheduleDto));
    }

    @Test
    void changeStatus_InvalidId_ThrowsNotFoundException() {
        EventRequestStatusDto eventRequestStatusDto = new EventRequestStatusDto("Not needed",
            EventRequestStatus.REJECTED, null);
        assertThrows(NotFoundException.class,
            () -> eventRequestService.changeStatus(0L, eventRequestStatusDto));
    }

    @Test
    void changeStatus_Approve_Successful() {
        eventRequest.setStatus(EventRequestStatus.REQUESTED);
        EventRequestStatusDto approveDto = new EventRequestStatusDto("Additional information...",
            EventRequestStatus.APPROVED, null);
        EventRequestReturnDto responseDto =
            new EventRequestReturnDto(1L, "Git training", "Git merging strategies", "APPROVED", 1L,
                1L);

        Email email = new Email(eventRequest.getRequester().getAccount().getEmail(), "Test",
            Map.of("fullName", "Test", "Test", "Test"));

        when(userService.getEntityById(any())).thenReturn(mentor);
        when(repository.findById(anyLong())).thenReturn(Optional.ofNullable(eventRequest));
        when(repository.save(any(EventRequest.class))).thenReturn(eventRequest);
        when(mapper.fromEventRequest(eventRequest)).thenReturn(responseDto);
        emailSendingJob.scheduleEmailJob(eventRequest.getRequester().getAccount().getEmail(), "Test", Map.of(
                "fullName", "Test", "Test", "Test"));

        EventRequestReturnDto returnDto = eventRequestService.changeStatus(1L, approveDto);

        assertThat(returnDto).usingRecursiveComparison().isEqualTo(responseDto);
    }

    @Test
    void changeStatus_ApproveAlreadyApproved_ThrowsEventRequestException() {
        eventRequest.setStatus(EventRequestStatus.APPROVED);
        EventRequestStatusDto approveDto = new EventRequestStatusDto("Additional information...",
            EventRequestStatus.APPROVED, null);

        when(repository.findById(anyLong())).thenReturn(Optional.ofNullable(eventRequest));

        Assertions.assertThrows(EventRequestException.class,
            () -> eventRequestService.changeStatus(1L, approveDto));
    }

    @Test
    void changeStatus_AssignMentor_Successful() {
        eventRequest.setStatus(EventRequestStatus.REQUESTED);
        EventRequestStatusDto assignedDto = new EventRequestStatusDto("Additional information...",
            EventRequestStatus.APPROVED, 2L);
        EventRequestReturnDto responseDto =
            new EventRequestReturnDto(1L, "Git training", "Git merging strategies", "APPROVED", 1L,
                2L, 1L);

        when(userService.getEntityById(mentor.getId())).thenReturn(mentor);
        when(repository.findById(anyLong())).thenReturn(Optional.ofNullable(eventRequest));
        when(userService.getEntityById(anyLong())).thenReturn(mentor);
        when(repository.save(any(EventRequest.class))).thenReturn(eventRequest);
        when(mapper.fromEventRequest(eventRequest)).thenReturn(responseDto);

        EventRequestReturnDto returnDto = eventRequestService.changeStatus(1L, assignedDto);

        assertThat(returnDto).usingRecursiveComparison().isEqualTo(responseDto);
    }

    @Test
    void changeStatus_AssignMentorWrongStatus_ThrowsEventRequestException() {
        eventRequest.setStatus(EventRequestStatus.REQUESTED);
        EventRequestStatusDto assignedDto = new EventRequestStatusDto("Additional information...",
            EventRequestStatus.REJECTED, 2L);

        when(repository.findById(anyLong())).thenReturn(Optional.ofNullable(eventRequest));

        assertThrows(EventRequestException.class,
            () -> eventRequestService.changeStatus(1L, assignedDto));
    }

    @Test
    void changeStatus_AssignMentorAssigneeNotMentor_ThrowsEventRequestException() {
        eventRequest.setStatus(EventRequestStatus.REQUESTED);
        EventRequestStatusDto assignedDto = new EventRequestStatusDto("Additional information...",
            EventRequestStatus.APPROVED, 1L);

        when(repository.findById(anyLong())).thenReturn(Optional.ofNullable(eventRequest));
        when(userService.getEntityById(anyLong())).thenReturn(intern);

        assertThrows(EventRequestException.class,
            () -> eventRequestService.changeStatus(1L, assignedDto));
    }

    @Test
    void changeStatus_StatusInDtoRequested_ThrowsEventRequestException() {
        EventRequestStatusDto approveDto = new EventRequestStatusDto("Additional information...",
            EventRequestStatus.REQUESTED, null);

        when(repository.findById(anyLong())).thenReturn(Optional.ofNullable(eventRequest));

        assertThrows(EventRequestException.class,
            () -> eventRequestService.changeStatus(1L, approveDto));
    }

    @Test
    void changeStatus_updateAssignee_Successful() {

        eventRequest.setStatus(EventRequestStatus.APPROVED);
        eventRequest.setAssignee(mentor);

        Account assigneeAccount = new Account();
        assigneeAccount.setId(3L);
        assigneeAccount.setEmail("test@mail.com");
        assigneeAccount.setPassword("1Password@");
        assigneeAccount.setRole(Role.MENTOR);

        User newAssignee = new User();
        newAssignee.setId(2L);
        newAssignee.setFullName("Mentor");
        newAssignee.setLocation("Mentor location");
        newAssignee.setAccount(assigneeAccount);

        EventRequestStatusDto eventRequestStatusDto = new EventRequestStatusDto("test",
            EventRequestStatus.APPROVED, newAssignee.getId());

        EventRequestReturnDto eventRequestReturnDto = new EventRequestReturnDto(5L,
            eventRequest.getTitle(),
            eventRequest.getDescription(), "APPROVED", eventRequest.getId(), newAssignee.getId());

        Email email = new Email(eventRequest.getRequester().getAccount().getEmail(), "Test",
            Map.of("fullName", "Test", "Test", "Test"));

        when(repository.findById(eventRequest.getId())).thenReturn(
            Optional.of(eventRequest));
        when(userService.getEntityById(eventRequestStatusDto.assignee())).thenReturn(newAssignee);
        emailSendingJob.scheduleEmailJob(eventRequest.getRequester().getAccount().getEmail(), "Test", Map.of(
                "fullName", "Test", "Test", "Test"));
        when(repository.save(eventRequest)).thenReturn(eventRequest);
        when(mapper.fromEventRequest(eventRequest)).thenReturn(eventRequestReturnDto);

        assertThat(eventRequestReturnDto).isEqualTo(
            eventRequestService.changeStatus(eventRequest.getId(),
                eventRequestStatusDto));

    }

    @Test
    void changeStatus_StatusInDtoApprovedAndAssigneeInDtoIsNotMentor_ThrowsEventRequestException() {

        eventRequest.setStatus(EventRequestStatus.APPROVED);
        eventRequest.setAssignee(mentor);

        EventRequestStatusDto eventRequestStatusDto = new EventRequestStatusDto("test",
            EventRequestStatus.APPROVED, intern.getId());

        when(repository.findById(eventRequest.getId())).thenReturn(Optional.of(eventRequest));
        when(userService.getEntityById(eventRequestStatusDto.assignee())).thenReturn(intern);

        assertThrows(EventRequestException.class,
            () -> eventRequestService.changeStatus(eventRequest.getId(), eventRequestStatusDto));

    }

    @Test
    void changeStatus_statusInDtoApprovedAndAssigneeIsNull_ThrowsEventRequestException() {

        eventRequest.setStatus(EventRequestStatus.APPROVED);
        eventRequest.setAssignee(null);

        EventRequestStatusDto eventRequestStatusDto = new EventRequestStatusDto("test",
            EventRequestStatus.APPROVED, null);

        when(repository.findById(anyLong())).thenReturn(Optional.of(eventRequest));

        assertThrows(EventRequestException.class,
            () -> eventRequestService.changeStatus(1L, eventRequestStatusDto));

    }
}
