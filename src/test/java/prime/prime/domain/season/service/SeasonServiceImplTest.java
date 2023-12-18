package prime.prime.domain.season.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import prime.prime.domain.account.entity.Account;
import prime.prime.domain.account.entity.AccountStatus;
import prime.prime.domain.account.models.AccountReturnDto;
import prime.prime.domain.role.Role;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.season.entity.SeasonDurationType;
import prime.prime.domain.season.mapper.SeasonMapper;
import prime.prime.domain.season.models.*;
import prime.prime.domain.season.repository.SeasonRepository;
import prime.prime.domain.season.repository.SeasonSpecification;
import prime.prime.domain.technology.entity.Technology;
import prime.prime.domain.technology.mapper.TechnologyNameToTechnologyEntity;
import prime.prime.domain.technology.models.TechnologyReturnDto;
import prime.prime.domain.user.entity.User;
import prime.prime.domain.user.models.UserReturnDto;
import prime.prime.domain.user.service.UserService;
import prime.prime.infrastructure.exception.InvalidDateException;
import prime.prime.infrastructure.exception.NotFoundException;
import prime.prime.infrastructure.exception.SeasonException;
import prime.prime.infrastructure.jobs.EmailSendingJob;
import prime.prime.infrastructure.jobs.SeasonReminderJob;
import prime.prime.infrastructure.security.ProgresoUserDetails;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeasonServiceImplTest {

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private SeasonMapper seasonMapper;

    @Mock
    private TechnologyNameToTechnologyEntity technologyNameToTechnology;

    @Mock
    private UserService userService;
    @Mock
    private EmailSendingJob emailSendingJob;
    @Mock
    private Scheduler scheduler;
    @Mock
    private SeasonReminderJob seasonReminderJob;
    @Mock
    private SeasonNotificationService seasonNotificationService;

    @InjectMocks
    private SeasonServiceImpl seasonService;
    private static SeasonUpdateDto updateDto;
    private static SeasonResponseDto responseDto;
    private static SeasonResponseDto secondResponseDto;
    private static Season season;
    private static Season secondSeason;
    private static User intern;
    private static User internWithMoreSeasons;
    private static User firstMentor;
    private static User secondMentor;
    private static User admin;
    private static UserReturnDto internReturnDto;
    private static UserReturnDto mentorReturnDto;
    private static UserReturnDto secondMentorReturnDto;
    private static UserReturnDto adminReturnDto;
    private static UserSeasonDto userSeasonDto;
    private static final Set<User> users = new HashSet<>();
    private static ProgresoUserDetails userDetails;

    @Value("${progreso.email.admin}")
    private String adminEmail;

    @BeforeEach
    void setUp() {
        Account adminAccount = new Account();
        adminAccount.setRole(Role.ADMIN);
        adminAccount.setEmail("admin@test.com");
        adminAccount.setId(3L);

        admin = new User();
        admin.setAccount(adminAccount);
        admin.setId(3L);
        admin.setFullName("Tom Tomas");

        updateDto = new SeasonUpdateDto("New name of season",
                6,
                SeasonDurationType.WEEKS.name(),
                LocalDate.now().plusMonths(3),
                LocalDate.now().plusYears(1),
                Set.of("Javascript"), null);

        SeasonCreateDto createDto = new SeasonCreateDto("Some season",
                6,
                SeasonDurationType.MONTHS.name(),
                LocalDate.now().plusDays(2),
                LocalDate.now().plusMonths(2),
                Set.of("Java", "Flutter"), null);

        SeasonCreateDto secondCreateDto = new SeasonCreateDto("Season 2",
                6,
                SeasonDurationType.MONTHS.name(),
                LocalDate.now().plusDays(2),
                LocalDate.now().plusMonths(2),
                Set.of("Java", "Flutter"), null);

        season = new Season();
        season.setId(1L);
        season.setName(createDto.name());
        season.setDurationType(SeasonDurationType.MONTHS);
        season.setDurationValue(createDto.durationValue());
        season.setStartDate(createDto.startDate());
        season.setEndDate(createDto.endDate());
        season.setTechnologies(Set.of(new Technology(1L, "Java"), new Technology(2L, "Flutter")));
        season.setOwner(admin);

        secondSeason = new Season();
        secondSeason.setId(2L);
        secondSeason.setName(secondCreateDto.name());
        secondSeason.setDurationType(SeasonDurationType.MONTHS);
        secondSeason.setDurationValue(secondCreateDto.durationValue());
        secondSeason.setStartDate(secondCreateDto.startDate());
        secondSeason.setEndDate(secondCreateDto.endDate());
        secondSeason.setTechnologies(Set.of(new Technology(1L, "Java"), new Technology(2L, "Flutter")));
        secondSeason.setOwner(admin);

        userSeasonDto = new UserSeasonDto(season.getId(), season.getName());

        Account internAccount = new Account();
        internAccount.setRole(Role.INTERN);
        internAccount.setEmail("intern@test.com");
        internAccount.setId(2L);
        internAccount.setStatus(AccountStatus.ACTIVE);

        Account internAccount2 = new Account();
        internAccount2.setId(3L);
        internAccount2.setRole(Role.INTERN);
        internAccount2.setStatus(AccountStatus.ACTIVE);

        AccountReturnDto internAccountReturnDto = new AccountReturnDto(internAccount.getId(),
                internAccount.getEmail(),
                internAccount.getRole(),
                internAccount.getStatus());

        intern = new User();
        intern.setAccount(internAccount);
        intern.setId(2L);
        intern.setLocation("Sofia");
        intern.setFullName("Ivan Ivanov");

        internWithMoreSeasons = new User();
        internWithMoreSeasons.setAccount(internAccount2);
        internWithMoreSeasons.setId(3L);
        internWithMoreSeasons.setLocation("Sofia");
        internWithMoreSeasons.setFullName("Mike Smith");
        internWithMoreSeasons.setSeasons(Set.of(season, new Season(), new Season(), new Season()));

        Account mentorAccount = new Account();
        mentorAccount.setRole(Role.MENTOR);
        mentorAccount.setEmail("mentor@test.com");
        mentorAccount.setId(1L);
        mentorAccount.setStatus(AccountStatus.ACTIVE);

        Account secondMentorAccount = new Account();
        secondMentorAccount.setRole(Role.MENTOR);
        secondMentorAccount.setEmail("mentor2@test.com");
        secondMentorAccount.setId(2L);
        secondMentorAccount.setStatus(AccountStatus.ACTIVE);

        AccountReturnDto mentorAccountReturnDto = new AccountReturnDto(mentorAccount.getId(),
                mentorAccount.getEmail(),
                mentorAccount.getRole(),
                mentorAccount.getStatus());

        firstMentor = new User();
        firstMentor.setAccount(mentorAccount);
        firstMentor.setId(1L);
        firstMentor.setLocation("Sofia");
        firstMentor.setFullName("Ivan Ivanov");

        secondMentor = new User();
        secondMentor.setAccount(secondMentorAccount);
        secondMentor.setId(2L);
        secondMentor.setLocation("Belgrade");
        secondMentor.setFullName("Filip Filipovic");

        AccountReturnDto adminAccountReturnDto = new AccountReturnDto(adminAccount.getId(),
                adminAccount.getEmail(),
                adminAccount.getRole(),
                adminAccount.getStatus());

        internReturnDto = new UserReturnDto(intern.getId(),
                intern.getFullName(),
                intern.getDateOfBirth(),
                intern.getPhoneNumber(),
                intern.getLocation(),
                Set.of(new TechnologyReturnDto(1L, "Java")),
                intern.getImagePath(),
                internAccountReturnDto,
                null,
                Set.of(userSeasonDto));

        mentorReturnDto = new UserReturnDto(firstMentor.getId(),
                firstMentor.getFullName(),
                firstMentor.getDateOfBirth(),
                firstMentor.getPhoneNumber(),
                firstMentor.getLocation(),
                Set.of(new TechnologyReturnDto(1L, "Java")),
                firstMentor.getImagePath(),
                mentorAccountReturnDto,
                null,
                Set.of(userSeasonDto));

        secondMentorReturnDto = new UserReturnDto(secondMentor.getId(),
                secondMentor.getFullName(),
                secondMentor.getDateOfBirth(),
                secondMentor.getPhoneNumber(),
                secondMentor.getLocation(),
                Set.of(new TechnologyReturnDto(1L, "Java")),
                secondMentor.getImagePath(),
                mentorAccountReturnDto,
                null,
                Set.of(userSeasonDto));

        adminReturnDto = new UserReturnDto(admin.getId(),
                admin.getFullName(),
                admin.getDateOfBirth(),
                admin.getPhoneNumber(),
                admin.getLocation(),
                null,
                admin.getImagePath(),
                adminAccountReturnDto,
                null,
                Set.of(userSeasonDto));

        responseDto = new SeasonResponseDto(season.getId(),
                season.getName(),
                season.getDurationValue(),
                season.getDurationType(),
                season.getStartDate(),
                season.getEndDate(),
                List.of(new TechnologyReturnDto(1L, "Java"), new TechnologyReturnDto(2L, "Flutter")),
                Set.of(internReturnDto),
                Set.of(mentorReturnDto),
                adminReturnDto
        );

        secondResponseDto = new SeasonResponseDto(season.getId(),
                secondSeason.getName(),
                secondSeason.getDurationValue(),
                secondSeason.getDurationType(),
                secondSeason.getStartDate(),
                secondSeason.getEndDate(),
                List.of(new TechnologyReturnDto(1L, "Java"), new TechnologyReturnDto(2L, "Flutter")),
                Set.of(internReturnDto),
                Set.of(mentorReturnDto, secondMentorReturnDto),
                adminReturnDto
        );

        updateDto = new SeasonUpdateDto("New name of season",
                6,
                SeasonDurationType.WEEKS.name(),
                LocalDate.now().plusMonths(3),
                LocalDate.now().plusYears(1),
                Set.of("Javascript"),
                Collections.singleton(firstMentor.getId()),
                Collections.singleton(intern.getId()),
                null);

        users.add(intern);
        users.add(firstMentor);
        season.setUsers(users);

        userDetails = new ProgresoUserDetails(adminAccount.getId(), adminAccount.getId(), adminAccount.getEmail()
                , adminAccount.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + adminAccount.getRole().name())));
    }

    @Test
    void create_ValidCreateDto_Successful() {
        SeasonCreateDto createDto = new SeasonCreateDto("Some season",
                6,
                SeasonDurationType.MONTHS.name(),
                LocalDate.now().plusDays(2),
                LocalDate.now().plusMonths(2),
                Set.of("Java", "Flutter"),
                Set.of(1L),
                Set.of(2L), null);

        Season noIdSeason = new Season();
        noIdSeason.setName(createDto.name());
        noIdSeason.setDurationType(SeasonDurationType.MONTHS);
        noIdSeason.setDurationValue(createDto.durationValue());
        noIdSeason.setStartDate(createDto.startDate());
        noIdSeason.setEndDate(createDto.endDate());
        noIdSeason.setTechnologies(
                Set.of(new Technology(1L, "Java"), new Technology(2L, "Flutter")));
        noIdSeason.setUsers(new HashSet<>(Set.of(firstMentor, intern)));

        firstMentor.setSeasons(new HashSet<>(Set.of(noIdSeason)));
        intern.setSeasons(new HashSet<>(Set.of(noIdSeason)));

        when(seasonMapper.fromCreateDto(createDto))
                .thenReturn(noIdSeason);

        when(userService.getAllById(Set.of(1L)))
                .thenReturn(Set.of(firstMentor));

        when(userService.getAllById(Set.of(2L)))
                .thenReturn(Set.of(intern));

        when(seasonRepository.save(noIdSeason))
                .thenReturn(any(Season.class));

        when(seasonMapper.toResponseDto(noIdSeason))
                .thenReturn(responseDto);
        when(userService.findByEmail(adminEmail)).thenReturn(admin);

        SeasonResponseDto result = seasonService.create(createDto);
        emailSendingJob.scheduleEmailJob("email@example.com", "Subject", new HashMap<>());

        assertThat(result).usingRecursiveComparison().isEqualTo(responseDto);
    }

    @Test
    void create_InvalidDate_ThrowsInvalidDateException() {
        SeasonCreateDto createDto = new SeasonCreateDto("as", 1,
                SeasonDurationType.MONTHS.name(), LocalDate.now(), LocalDate.now(), Set.of("Java"), null);

        when(seasonMapper.fromCreateDto(createDto))
                .thenThrow(InvalidDateException.class);

        assertThrows(InvalidDateException.class, () -> seasonService.create(createDto));
    }

    @Test
    void create_InvalidAssigneeRole_ThrowsSeasonException() {
        SeasonCreateDto createDto = new SeasonCreateDto("as",
                1,
                SeasonDurationType.MONTHS.name(),
                LocalDate.now(),
                LocalDate.now(),
                Set.of("Java"),
                Set.of(2L),
                null,
                null);

        when(seasonMapper.fromCreateDto(createDto))
                .thenReturn(season);

        when(userService.getAllById(Set.of(2L)))
                .thenReturn(Set.of(intern));

        assertThrows(SeasonException.class, () -> seasonService.create(createDto));
    }

    @Test
    void create_InvalidAccountStatus_ThrowsSeasonException() {
        SeasonCreateDto createDto = new SeasonCreateDto("as",
                1,
                SeasonDurationType.MONTHS.name(),
                LocalDate.now(),
                LocalDate.now(),
                Set.of("Java"),
                null,
                Set.of(2L),
                null);
        intern.getAccount().setStatus(AccountStatus.ARCHIVED);

        when(seasonMapper.fromCreateDto(createDto))
                .thenReturn(season);

        when(userService.getAllById(Set.of(2L)))
                .thenReturn(Set.of(intern));

        assertThrows(SeasonException.class, () -> seasonService.create(createDto));
    }

    @Test
    void create_InternMaxSeasonLimit_ThrowsSeasonException() {
        Account account = new Account();
        account.setId(3L);
        account.setRole(Role.INTERN);
        account.setStatus(AccountStatus.ACTIVE);
        internWithMoreSeasons.setAccount(account);

        SeasonCreateDto createDto = new SeasonCreateDto("as",
                1,
                SeasonDurationType.MONTHS.name(),
                LocalDate.now(),
                LocalDate.now(),
                Set.of("Java"),
                null,
                Set.of(internWithMoreSeasons.getId()),
                null);

        when(seasonMapper.fromCreateDto(createDto))
                .thenReturn(season);

        when(userService.findByEmail(adminEmail)).thenReturn(admin);
        when(userService.getAllById(Set.of(3L)))
                .thenReturn(Set.of(internWithMoreSeasons));

        assertThrows(SeasonException.class, () -> seasonService.create(createDto));
    }

    @Test
    void getById_ValidId_Success() {
        when(seasonRepository.findByIdAndFetchUsers(season.getId()))
                .thenReturn(Optional.of(season));

        when(seasonMapper.toResponseDto(season))
                .thenReturn(responseDto);

        SeasonResponseDto result = seasonService.getById(season.getId());

        assertThat(result).usingRecursiveComparison().isEqualTo(responseDto);
    }

    @Test
    void getById_InvalidId_ThrowsNotFoundException() {
        when(seasonRepository.findByIdAndFetchUsers(season.getId()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> seasonService.getById(1L));
    }

    @Test
    void getAll_Valid_Successful() {

        SearchSeasonDto searchSeasonDto = new SearchSeasonDto("1");
        Page<Season> page = new PageImpl<>(List.of(season));

        when(seasonRepository.findAll(any(SeasonSpecification.class), any(Pageable.class)))
                .thenReturn(page);

        when(seasonMapper.toResponseDto(season))
                .thenReturn(responseDto);

        Page<SeasonResponseDto> result = seasonService.getAll(Pageable.ofSize(20), userDetails, searchSeasonDto);

        assertEquals(page.getTotalElements(), result.getTotalElements());
        assertEquals(page.getTotalPages(), result.getTotalPages());
        assertFalse(result.isEmpty());
    }

    @Test
    void getAll_EmptyPage_Successful() {

        SearchSeasonDto searchSeasonDto = new SearchSeasonDto("1");

        Page<Season> seasons = new PageImpl<>(new ArrayList<>());
        when(seasonRepository.findAll(any(SeasonSpecification.class), any(Pageable.class)))
                .thenReturn(seasons);

        Page<SeasonResponseDto> result = seasonService.getAll(Pageable.ofSize(20),userDetails, searchSeasonDto);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAll_GroupByMentorId_ByAdmin() {
        SearchSeasonDto searchSeasonDto = new SearchSeasonDto("[1,2]");

        Page<Season> seasons  = new PageImpl<>(List.of(season, secondSeason));
        when(seasonRepository.findAll(any(SeasonSpecification.class), any(Pageable.class))).thenReturn(seasons);
        when(seasonMapper.toResponseDto(season)).thenReturn(responseDto);
        when(seasonMapper.toResponseDto(secondSeason)).thenReturn(secondResponseDto);

        Page<SeasonResponseDto> result = seasonService.getAll(Pageable.ofSize(20), userDetails, searchSeasonDto);

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void getAll_SortByMentorId_ByAdmin() {
        SearchSeasonDto searchSeasonDto = new SearchSeasonDto("[1]");

        Page<Season> seasons  = new PageImpl<>(List.of(season));
        when(seasonRepository.findAll(any(SeasonSpecification.class), any(Pageable.class))).thenReturn(seasons);
        when(seasonMapper.toResponseDto(season)).thenReturn(responseDto);

        Page<SeasonResponseDto> result = seasonService.getAll(Pageable.ofSize(20), userDetails, searchSeasonDto);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void update_ValidUpdateDto_Successful() {
        when(seasonRepository.findByIdAndFetchUsers(season.getId()))
                .thenReturn(Optional.of(season));

        when(technologyNameToTechnology.toTechnologyEntity(updateDto.technologies()))
                .thenReturn(Set.of(new Technology(3L, "Javascript")));

        doNothing().when(seasonMapper).updateFromUpdateDto(updateDto, season);

        season.setName(updateDto.name());
        season.setDurationType(SeasonDurationType.WEEKS);
        season.setDurationValue(updateDto.durationValue());
        season.setStartDate(updateDto.startDate());
        season.setEndDate(updateDto.endDate());
        season.setTechnologies(Set.of(new Technology(3L, "Javascript")));
        season.setUsers(new HashSet<>(Set.of(firstMentor, intern)));
        season.setOwner(admin);

        SeasonResponseDto newResponseDto = new SeasonResponseDto(season.getId(),
                season.getName(),
                season.getDurationValue(),
                season.getDurationType(),
                season.getStartDate(),
                season.getEndDate(),
                List.of(new TechnologyReturnDto(3L, "Javascript")),
                Set.of(mentorReturnDto),
                Set.of(internReturnDto),
                adminReturnDto);

        when(seasonRepository.save(season))
                .thenReturn(season);

        when(seasonMapper.toResponseDto(season))
                .thenReturn(newResponseDto);
        when(userService.findByEmail(adminEmail)).thenReturn(admin);

        SeasonResponseDto result = seasonService.update(season.getId(), updateDto);
        emailSendingJob.scheduleEmailJob("email@example.com", "Subject", new HashMap<>());

        assertThat(result).usingRecursiveComparison().isEqualTo(newResponseDto);
    }

    @Test
    void update_InvalidId_ThrowsNotFoundException() {
        when(seasonRepository.findByIdAndFetchUsers(season.getId()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> seasonService.update(1L, updateDto));
    }

    @Test
    void update_InvalidDate_ThrowsInvalidDateException() {
        when(seasonRepository.findByIdAndFetchUsers(season.getId()))
                .thenReturn(Optional.of(season));

        SeasonUpdateDto seasonUpdateDto = new SeasonUpdateDto("New name of season",
                6,
                SeasonDurationType.WEEKS.name(),
                LocalDate.now().plusMonths(3),
                LocalDate.now().plusMonths(2),
                Set.of("Javascript"), null, null, null);

        doThrow(InvalidDateException.class).when(seasonMapper)
                .updateFromUpdateDto(seasonUpdateDto, season);

        assertThrows(InvalidDateException.class, () -> seasonService.update(1L, seasonUpdateDto));
    }

    @Test
    void update_InvalidAssigneeRole_ThrowsSeasonException() {
        when(seasonRepository.findByIdAndFetchUsers(season.getId()))
                .thenReturn(Optional.of(season));

        SeasonUpdateDto invalidUpdateDto = new SeasonUpdateDto("as",
                1,
                SeasonDurationType.MONTHS.name(),
                LocalDate.now(),
                LocalDate.now(),
                Set.of("Java"),
                Set.of(2L),
                null,
                null);

        when(userService.getAllById(Set.of(2L)))
                .thenReturn(Set.of(intern));

        assertThrows(SeasonException.class, () -> seasonService.update(1L, invalidUpdateDto));
    }

    @Test
    void update_InvalidAccountStatus_ThrowsSeasonException() {
        when(seasonRepository.findByIdAndFetchUsers(season.getId()))
                .thenReturn(Optional.of(season));

        SeasonUpdateDto invalidUpdateDto = new SeasonUpdateDto("as",
                1,
                SeasonDurationType.MONTHS.name(),
                LocalDate.now(),
                LocalDate.now(),
                Set.of("Java"),
                null,
                Set.of(2L),
                null);
        intern.getAccount().setStatus(AccountStatus.ARCHIVED);

        when(userService.getAllById(Set.of(2L)))
                .thenReturn(Set.of(intern));

        assertThrows(SeasonException.class, () -> seasonService.update(1L, invalidUpdateDto));
    }

    @Test
    void update_InternMaxSeasonLimit_ThrowsSeasonException() {
        when(seasonRepository.findByIdAndFetchUsers(season.getId()))
                .thenReturn(Optional.of(season));

        SeasonUpdateDto seasonUpdateDto = new SeasonUpdateDto("as",
                1,
                SeasonDurationType.MONTHS.name(),
                LocalDate.now(),
                LocalDate.now(),
                Set.of("Java"),
                Set.of(1L),
                Set.of(3L),
                null);
        Set<Long> ids = new HashSet<>();
        ids.add(1L);

        when(userService.getAllById(ids))
                .thenReturn(Set.of(internWithMoreSeasons));

        assertThrows(SeasonException.class, () -> seasonService.update(1L, seasonUpdateDto));
    }

    @Test
    void delete_ValidId_Successful() {
        when(seasonRepository.findByIdAndFetchUsers(season.getId())).thenReturn(
                Optional.of(season));

        seasonService.delete(season.getId());

        verify(seasonRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_InvalidId_ThrowsNotFoundException() {
        when(seasonRepository.findByIdAndFetchUsers(season.getId()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> seasonService.delete(1L));
    }

    @Test
    void findById_ValidId_Successful() {
        when(seasonRepository.findById(season.getId())).thenReturn(Optional.of(season));

        Season returnedSeason = seasonService.findById(season.getId());

        assertEquals("Some season", returnedSeason.getName());
        assertEquals(6, returnedSeason.getDurationValue());
        assertEquals(SeasonDurationType.MONTHS, returnedSeason.getDurationType());
    }

    @Test
    void findById_InvalidId_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class, () -> seasonService.findById(10L));
    }

    @Test
    void findActiveSeason_ExistingSeasonsIntern_Successful() {
        Season newSeason = new Season();
        newSeason.setId(2L);
        newSeason.setDurationType(SeasonDurationType.MONTHS);
        newSeason.setDurationValue(6);
        newSeason.setStartDate(LocalDate.now().minusDays(1));
        newSeason.setEndDate(LocalDate.now().plusMonths(6));
        intern.setSeasons(Set.of(newSeason));

        Season returnedSeason = seasonService.findActiveSeason(intern, 2L);

        assertEquals(2L, returnedSeason.getId());
    }

    @Test
    void findActiveSeason_NotActiveSeasonIntern_ThrowsSeasonException() {
        intern.setSeasons(Set.of(season));
        assertThrows(SeasonException.class, () -> seasonService.findActiveSeason(intern, 1L));
    }

    @Test
    void findActiveSeason_NonExistingSeasonsIntern_ThrowsSeasonException() {
        intern.setSeasons(null);
        assertThrows(SeasonException.class, () -> seasonService.findActiveSeason(intern, 1L));
    }

    @Test
    void findActiveSeason_ActiveSeasonAdmin_Successful() {
        Season newSeason = new Season();
        newSeason.setId(2L);
        newSeason.setDurationType(SeasonDurationType.MONTHS);
        newSeason.setDurationValue(6);
        newSeason.setStartDate(LocalDate.now().minusDays(1));
        newSeason.setEndDate(LocalDate.now().plusMonths(6));
        when(seasonRepository.findById(newSeason.getId())).thenReturn(Optional.of(newSeason));

        Season returnedSeason = seasonService.findActiveSeason(admin, 2L);

        assertEquals(2L, returnedSeason.getId());
    }
}