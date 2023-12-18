package prime.prime.domain.absence.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
import prime.prime.domain.absence.entity.AbsenceRequest;
import prime.prime.domain.absence.entity.AbsenceRequestStatus;
import prime.prime.domain.absence.entity.AbsenceRequestType;
import prime.prime.domain.absence.mapper.AbsenceRequestMapper;
import prime.prime.domain.absence.models.*;
import prime.prime.domain.absence.repository.AbsenceRepository;
import prime.prime.domain.absence.repository.AbsenceRequestSpecification;
import prime.prime.domain.absence.repository.CalendarAbsenceSpecification;
import prime.prime.domain.account.entity.Account;
import prime.prime.domain.role.Role;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.season.service.SeasonService;
import prime.prime.domain.user.entity.User;
import prime.prime.domain.user.service.UserService;
import prime.prime.infrastructure.email_sender.config.Email;
import prime.prime.infrastructure.exception.AbsenceException;
import prime.prime.infrastructure.exception.InvalidDateException;
import prime.prime.infrastructure.exception.NotFoundException;
import prime.prime.infrastructure.jobs.EmailSendingJob;
import prime.prime.infrastructure.security.ProgresoUserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AbsenceTrackingServiceImplTest {

    @Mock
    private UserService userService;
    @Mock
    private AbsenceRepository absenceRepository;
    @Mock
    private AbsenceRequestMapper absenceRequestMapper;
    @Mock
    private SeasonService seasonService;
    @Mock
    private EmailSendingJob emailSendingJob;

    @InjectMocks
    private AbsenceTrackingServiceImpl absenceTrackingService;

    private static AbsenceRequest absenceRequest;
    private static AbsenceRequestDto absenceRequestDto;
    private static AbsenceResponseDto absenceResponseDto;
    private static Account account;
    private static User intern;
    private static Set<Season> seasons;
    private static Season season;
    private static Email email;
    private static ProgresoUserDetails userDetails;
    private static AbsenceRequestStatusDto absenceRequestStatusDto;
    private static Long absenceRequestId;
    private static AbsenceRequestUpdateDto absenceRequestUpdateDto;

    @BeforeEach
    void setUp() {
        Role role = Role.INTERN;

        account = new Account();
        account.setId(1L);
        account.setEmail("test@mail.com");
        account.setPassword("1Password@");
        account.setRole(role);

        seasons = new HashSet<>();
        season = new Season();
        season.setId(1L);
        season.setStartDate(LocalDate.now().minusDays(1));
        season.setEndDate(LocalDate.now().plusMonths(6));
        seasons.add(season);

        intern = new User();
        intern.setId(1L);
        intern.setFullName("Cool Name");
        intern.setLocation("Cool Location");
        intern.setAccount(account);
        intern.setSeasons(seasons);

        absenceRequest = new AbsenceRequest();
        absenceRequest.setId(1L);
        absenceRequest.setTitle("Test");
        absenceRequest.setDescription("Test");
        absenceRequest.setStatus(AbsenceRequestStatus.PENDING);
        absenceRequest.setRequester(intern);
        absenceRequest.setSeason(season);
        absenceRequest.setAbsenceType(AbsenceRequestType.SICK_LEAVE);
        absenceRequest.setStartTime(LocalDateTime.now().plusDays(10));
        absenceRequest.setEndTime(LocalDateTime.now().plusDays(30));

        absenceRequestDto = new AbsenceRequestDto(
                "Test",
                "Test",
                AbsenceRequestType.SICK_LEAVE,
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(30),
                season.getId());

        absenceResponseDto = new AbsenceResponseDto(
                absenceRequest.getId(),
                absenceRequest.getTitle(),
                absenceRequest.getDescription(),
                absenceRequest.getStatus().name(),
                absenceRequest.getRequester().getId(),
                absenceRequest.getAbsenceType().name(),
                absenceRequest.getStartTime(),
                absenceRequest.getEndTime(),
                absenceRequest.getSeason().getId()
        );

         absenceRequestStatusDto = new AbsenceRequestStatusDto(
                "Comment",
                 AbsenceRequestStatus.APPROVED
        );

         email = new Email(absenceRequest.getRequester().getAccount().getEmail(), "Test",
                Map.of("fullName", "Test", "Test", "Test"));

         userDetails = new ProgresoUserDetails(account.getId(), intern.getId(), account.getEmail()
                , account.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + account.getRole().name())));

         absenceRequestId = absenceRequest.getId();

         absenceRequestUpdateDto = new AbsenceRequestUpdateDto(
                 "Test","test",
                 LocalDateTime.now().plusDays(2),
                 LocalDateTime.now().plusDays(15));
    }

    @Test
    void createAbsenceRequest_ValidAbsenceRequestDto_Successful() {
        when(userService.getEntityById(anyLong())).thenReturn(intern);
        when(absenceRequestMapper.toAbsenceRequest(absenceRequestDto)).thenReturn(absenceRequest);
        when(seasonService.findActiveSeason(intern, season.getId())).thenReturn(season);
        when(absenceRepository.numberOfAbsencesForGivenDatesAndEmployee(intern.getId(), season.getId(),
                absenceRequest.getStartTime(), absenceRequest.getEndTime())).thenReturn(0L);
        emailSendingJob.scheduleEmailJob(absenceRequest.getRequester().getAccount().getEmail(), "Test", Map.of(
                "fullName", "Test", "Test", "Test"));
        when(absenceRepository.save(any(AbsenceRequest.class))).thenReturn(absenceRequest);
        when(absenceRequestMapper.toAbsenceResponse(absenceRequest)).thenReturn(absenceResponseDto);

        AbsenceResponseDto responseDto = absenceTrackingService.createAbsenceRequest(absenceRequestDto, userDetails);

        assertThat(responseDto).usingRecursiveComparison().isEqualTo(absenceResponseDto);
    }

    @Test
    void createAbsenceRequest_RequestedDateNotWithinSeason_ThrowsInvalidDateException() {
        AbsenceRequestDto absenceRequestDto = new AbsenceRequestDto(
                "Test",
                "Test",
                AbsenceRequestType.SICK_LEAVE,
                LocalDateTime.now().plusMonths(7),
                LocalDateTime.now().plusMonths(7),
                season.getId());

        when(userService.getEntityById(anyLong())).thenReturn(intern);
        when(absenceRequestMapper.toAbsenceRequest(absenceRequestDto)).thenReturn(absenceRequest);
        when(seasonService.findActiveSeason(intern, season.getId())).thenReturn(season);

        assertThrows(InvalidDateException.class, () -> absenceTrackingService.createAbsenceRequest(absenceRequestDto, userDetails));
    }

    @Test
    void createAbsenceRequest_AlreadyRequestedDate_ThrowsAbsenceException() {
        when(userService.getEntityById(anyLong())).thenReturn(intern);
        when(absenceRequestMapper.toAbsenceRequest(absenceRequestDto)).thenReturn(absenceRequest);
        when(seasonService.findActiveSeason(intern, season.getId())).thenReturn(season);
        when(absenceRepository.numberOfAbsencesForGivenDatesAndEmployee(intern.getId(), season.getId(),
                absenceRequestDto.startTime(), absenceRequestDto.endTime())).thenReturn(1L);

        assertThrows(AbsenceException.class, () -> absenceTrackingService.createAbsenceRequest(absenceRequestDto,userDetails));
    }

    @Test
    void changeStatus_ValidAbsenceRequestStatusDto_Successful() {
       AbsenceRequestStatusDto absenceRequestStatusDto = new AbsenceRequestStatusDto(
                "Comment",
                AbsenceRequestStatus.APPROVED
        );

       AbsenceResponseDto absenceResponseDto = new AbsenceResponseDto(
                absenceRequest.getId(),
                absenceRequest.getTitle(),
                absenceRequest.getDescription(),
                AbsenceRequestStatus.APPROVED.toString(),
                absenceRequest.getRequester().getId(),
                absenceRequest.getAbsenceType().name(),
                absenceRequest.getStartTime(),
                absenceRequest.getEndTime(),
                absenceRequest.getSeason().getId()
        );
        when(absenceRepository.findById(absenceRequest.getId())).thenReturn(Optional.of(absenceRequest));
        when(absenceRepository.save(absenceRequest)).thenReturn(absenceRequest);
        emailSendingJob.scheduleEmailJob(absenceRequest.getRequester().getAccount().getEmail(), "Test", Map.of(
                "fullName", "Test", "Test", "Test"));
        when(absenceRequestMapper.toAbsenceResponse(absenceRequest)).thenReturn(absenceResponseDto);

        AbsenceResponseDto responseDto = absenceTrackingService.changeStatus(absenceRequest.getId(),absenceRequestStatusDto);

        assertThat(responseDto).usingRecursiveComparison().isEqualTo(absenceResponseDto);

    }

    @Test
    void changeStatus_AbsenceRequestNotFound_ThrowsNotFoundException() {
        when(absenceRepository.findById(absenceRequest.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> absenceTrackingService.changeStatus(absenceRequestId,absenceRequestStatusDto));
    }

    @Test
    void changeStatus_StatusAlreadyApproved_ThrowsAbsenceException() {
        absenceRequest.setStatus(AbsenceRequestStatus.APPROVED);
        when(absenceRepository.findById(absenceRequest.getId())).thenReturn(Optional.of(absenceRequest));


        assertThrows(AbsenceException.class, () -> absenceTrackingService.changeStatus(absenceRequestId,absenceRequestStatusDto));
    }

    @Test
    void changeStatus_StatusAlreadyRejected_ThrowsAbsenceException() {
        absenceRequest.setStatus(AbsenceRequestStatus.REJECTED);
        when(absenceRepository.findById(absenceRequest.getId())).thenReturn(Optional.of(absenceRequest));

        assertThrows(AbsenceException.class, () -> absenceTrackingService.changeStatus(absenceRequestId,absenceRequestStatusDto));
    }

    @Test
    void changeStatus_OneDayNotice_ThrowsAbsenceException() {
        absenceRequest.setStartTime(LocalDateTime.now());
        when(absenceRepository.findById(absenceRequest.getId())).thenReturn(Optional.of(absenceRequest));

        assertThrows(AbsenceException.class, () -> absenceTrackingService.changeStatus(absenceRequestId,absenceRequestStatusDto));
    }

    @Test
    void getById_ValidId_Successful() {
        when(absenceRepository.findById(anyLong())).thenReturn(Optional.ofNullable(absenceRequest));
        when(absenceRequestMapper.toAbsenceResponse(absenceRequest)).thenReturn(absenceResponseDto);

        AbsenceResponseDto actual = absenceTrackingService.getById(1L);

        assertThat(actual).usingRecursiveComparison().isEqualTo(absenceResponseDto);
    }

    @Test
    void getById_InvalidId_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class, () -> absenceTrackingService.getById(0L));
    }


    @ParameterizedTest
    @MethodSource("searchAbsenceRequestDtoProvider")
    void getAll_differentFilterValues_Successful(SearchAbsenceRequestDto searchAbsenceRequestDto) {
        AbsenceRequest absenceRequest = new AbsenceRequest();
        absenceRequest.setId(2L);
        absenceRequest.setTitle("Test");
        absenceRequest.setDescription("Test");
        absenceRequest.setStatus(AbsenceRequestStatus.APPROVED);
        absenceRequest.setRequester(intern);
        absenceRequest.setSeason(season);
        absenceRequest.setAbsenceType(AbsenceRequestType.SICK_LEAVE);
        absenceRequest.setStartTime(LocalDateTime.now().plusDays(10));
        absenceRequest.setEndTime(LocalDateTime.now().plusDays(30));

        AbsenceResponseDto absenceResponseDto = new AbsenceResponseDto(
                absenceRequest.getId(),
                absenceRequest.getTitle(),
                absenceRequest.getDescription(),
                absenceRequest.getStatus().name(),
                absenceRequest.getRequester().getId(),
                absenceRequest.getAbsenceType().name(),
                absenceRequest.getStartTime(),
                absenceRequest.getEndTime(),
                absenceRequest.getSeason().getId());

        Pageable pageable = PageRequest.of(0,1);

        when(absenceRepository.findAll(any(AbsenceRequestSpecification.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(List.of(absenceRequest)));
        when(absenceRequestMapper.toAbsenceResponse(absenceRequest)).thenReturn(absenceResponseDto);
        when(userService.getEntityById(anyLong())).thenReturn(intern);

        Page<AbsenceResponseDto> absenceResponseDtos = absenceTrackingService.getAll(
                searchAbsenceRequestDto, pageable, userDetails);

        assertThat(absenceResponseDtos.getContent().get(0)).usingRecursiveComparison()
                .isEqualTo(absenceResponseDto);
    }

    private static List<SearchAbsenceRequestDto> searchAbsenceRequestDtoProvider() {
        return List.of(
                new SearchAbsenceRequestDto("APPROVED", "1L", null),
                new SearchAbsenceRequestDto("APPROVED", null, null),
                new SearchAbsenceRequestDto(null, "1L", null),
                new SearchAbsenceRequestDto(null, "1L", "SICK_LEAVE"),
                new SearchAbsenceRequestDto(null, null, "SICK_LEAVE"),
                new SearchAbsenceRequestDto("APPROVED", "1L", "SICK_LEAVE")
        );
    }

    @Test
    void getAll_EmptyPage_Successful() {
        when(absenceRepository.findAll(any(AbsenceRequestSpecification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 1), 0));

        SearchAbsenceRequestDto searchAbsenceRequestDto =
                new SearchAbsenceRequestDto("REJECTED","1L","SICK_LEAVE");
        Pageable pageable = PageRequest.of(0,1);

        when(userService.getEntityById(anyLong())).thenReturn(intern);
        Page<AbsenceResponseDto> absenceResponseDtos = absenceTrackingService.getAll(
                searchAbsenceRequestDto, pageable, userDetails);

        assertEquals(0, absenceResponseDtos.getTotalPages());
        assertEquals(0, absenceResponseDtos.getTotalElements());
        assertNotNull(absenceResponseDtos.getContent());
    }

    @Test
    void updateAbsenceRequest_ValidAbsenceRequestUpdateDto_Successful() {

       AbsenceResponseDto absenceResponseDto = new AbsenceResponseDto(
                absenceRequest.getId(),
                absenceRequestUpdateDto.title(),
                absenceRequestUpdateDto.description(),
                absenceRequest.getStatus().name(),
                absenceRequest.getRequester().getId(),
                absenceRequest.getAbsenceType().name(),
                absenceRequestUpdateDto.startTime(),
                absenceRequestUpdateDto.endTime(),
                absenceRequest.getSeason().getId()
        );
        when(userService.getEntityById(anyLong())).thenReturn(intern);
        when(absenceRepository.findById(anyLong())).thenReturn(Optional.of(absenceRequest));
        when(seasonService.findById(absenceRequest.getSeason().getId())).thenReturn(season);
        when(seasonService.findActiveSeason(intern, season.getId())).thenReturn(season);

        when(absenceRepository.save(any(AbsenceRequest.class))).thenReturn(absenceRequest);
        when(absenceRequestMapper.toAbsenceResponse(absenceRequest)).thenReturn(absenceResponseDto);

        AbsenceResponseDto responseDto = absenceTrackingService.update(1L, absenceRequestUpdateDto,userDetails);

        assertThat(responseDto).usingRecursiveComparison().isEqualTo(absenceResponseDto);
    }

    @Test
    void updateAbsenceRequest_ValidAbsenceRequestUpdateDtoWithStartTimeOnly_Successful() {

        AbsenceRequestUpdateDto absenceRequestUpdateDto = new AbsenceRequestUpdateDto(
                "Test","test",
                LocalDateTime.now().plusMonths(2),
                null);

        AbsenceResponseDto absenceResponseDto = new AbsenceResponseDto(
                absenceRequest.getId(),
                absenceRequestUpdateDto.title(),
                absenceRequestUpdateDto.description(),
                absenceRequest.getStatus().name(),
                absenceRequest.getRequester().getId(),
                absenceRequest.getAbsenceType().name(),
                absenceRequestUpdateDto.startTime(),
                absenceRequestUpdateDto.endTime(),
                absenceRequest.getSeason().getId()
        );
        when(userService.getEntityById(anyLong())).thenReturn(intern);
        when(absenceRepository.findById(anyLong())).thenReturn(Optional.of(absenceRequest));
        when(seasonService.findById(absenceRequest.getSeason().getId())).thenReturn(season);
        when(seasonService.findActiveSeason(intern, season.getId())).thenReturn(season);

        when(absenceRepository.save(any(AbsenceRequest.class))).thenReturn(absenceRequest);
        when(absenceRequestMapper.toAbsenceResponse(absenceRequest)).thenReturn(absenceResponseDto);

        AbsenceResponseDto responseDto = absenceTrackingService.update(1L, absenceRequestUpdateDto,userDetails);

        assertThat(responseDto).usingRecursiveComparison().isEqualTo(absenceResponseDto);
    }

    @Test
    void updateAbsenceRequest_InvalidId_Unsuccessful() {
        assertThrows(NotFoundException.class, () -> absenceTrackingService.update(3L, absenceRequestUpdateDto,userDetails));
    }

    @Test
    void updateAbsenceRequest_RequestedDateNotWithinSeason_ThrowsInvalidDateException() {
       AbsenceRequestUpdateDto absenceRequestUpdateDto = new AbsenceRequestUpdateDto(
                "Test","test",
                LocalDateTime.now().plusMonths(7),
                LocalDateTime.now().plusMonths(7));

        when(userService.getEntityById(anyLong())).thenReturn(intern);
        when(absenceRepository.findById(anyLong())).thenReturn(Optional.of(absenceRequest));
        when(seasonService.findById(absenceRequest.getSeason().getId())).thenReturn(season);
        when(seasonService.findActiveSeason(intern, season.getId())).thenReturn(season);

        assertThrows(InvalidDateException.class, () -> absenceTrackingService.update(1L, absenceRequestUpdateDto,userDetails));
    }

    @Test
    void updateAbsenceRequest_RequestedDateNotWithinSeasonWithStartTimeOnly_ThrowsInvalidDateException() {
        AbsenceRequestUpdateDto absenceRequestUpdateDto = new AbsenceRequestUpdateDto(
                "Test","test",
                LocalDateTime.now().plusMonths(7),null);

        when(userService.getEntityById(anyLong())).thenReturn(intern);
        when(absenceRepository.findById(anyLong())).thenReturn(Optional.of(absenceRequest));
        when(seasonService.findById(absenceRequest.getSeason().getId())).thenReturn(season);
        when(seasonService.findActiveSeason(intern, season.getId())).thenReturn(season);

        assertThrows(InvalidDateException.class, () -> absenceTrackingService.update(1L, absenceRequestUpdateDto,userDetails));
    }

    @Test
    void delete_ValidId_Successful() {
        when(absenceRepository.existsById(anyLong())).thenReturn(true);

        absenceTrackingService.delete(absenceRequest.getId());

        verify(absenceRepository).deleteById(absenceRequest.getId());
    }

    @Test
    void delete_InvalidId_IdNotFound() {
        assertThrows(NotFoundException.class,
                () -> absenceTrackingService.delete(3L));
    }

    @Test
    void getAbsencesByDateFor_NoAbsencesInGivenDateSpan() {

        var startTime = LocalDateTime.now();
        var endTime = LocalDateTime.now().plusDays(20);
        SearchCalendarAbsenceRequestDto calendarAbsenceRequestDto =
                new SearchCalendarAbsenceRequestDto("PENDING", "SICK_LEAVE");

        when(userService.getEntityById(anyLong())).thenReturn(intern);
        when(absenceRepository.getAbsenceIdsByDateForUser(startTime, endTime, List.of(1L))).
                thenReturn(null);

        when(absenceRequestMapper.toCalendarAbsences(absenceRequest)).thenReturn(null);
        when(absenceRepository.findAll(any(CalendarAbsenceSpecification.class))).thenReturn(List.of(absenceRequest));

        var result = absenceTrackingService.getAbsencesByDate(startTime.toLocalDate(), endTime.toLocalDate(), calendarAbsenceRequestDto, userDetails);

        assertTrue(result.contains(null));
    }

    @Test
    void getAbsencesByDate_Successful() {

        var startTime = LocalDateTime.now();
        var endTime = LocalDateTime.now().plusDays(60);
        CalendarAbsenceResponseDto calendarAbsenceResponseDto =
                new CalendarAbsenceResponseDto(1L, "Cool Name - SICK_LEAVE", "APPROVED", "SICK_LEAVE",
                        startTime, endTime);

        SearchCalendarAbsenceRequestDto calendarAbsenceRequestDto =
                new SearchCalendarAbsenceRequestDto("APPROVED", "SICK_LEAVE");

        when(userService.getEntityById(anyLong())).thenReturn(intern);
        when(absenceRepository.getAbsenceIdsByDateForUser(startTime, endTime, List.of(1L))).
                thenReturn(List.of(absenceRequest.getId()));

        when(absenceRequestMapper.toCalendarAbsences(absenceRequest)).thenReturn(calendarAbsenceResponseDto);
        when(absenceRepository.findAll(any(CalendarAbsenceSpecification.class))).thenReturn(List.of(absenceRequest));

        var result = absenceTrackingService.getAbsencesByDate(startTime.toLocalDate(), endTime.toLocalDate(), calendarAbsenceRequestDto, userDetails);

        assertEquals("Cool Name - SICK_LEAVE", result.get(0).displayName());
    }
}