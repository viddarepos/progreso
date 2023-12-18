package prime.prime.infrastructure.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import prime.prime.domain.absence.models.AbsenceResponseDto;
import prime.prime.domain.absence.service.AbsenceTrackingService;
import prime.prime.domain.account.entity.Account;
import prime.prime.domain.account.entity.AccountStatus;
import prime.prime.domain.account.models.AccountCreateDto;
import prime.prime.domain.account.models.AccountReturnDto;
import prime.prime.domain.account.models.AccountUpdateDto;
import prime.prime.domain.event.models.AttendeesDto;
import prime.prime.domain.event.models.EventResponseWithAttendeesDto;
import prime.prime.domain.event.service.EventServiceImpl;
import prime.prime.domain.eventrequest.models.EventRequestReturnDto;
import prime.prime.domain.eventrequest.service.EventRequestServiceImpl;
import prime.prime.domain.role.Role;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.season.entity.SeasonDurationType;
import prime.prime.domain.season.models.SeasonResponseDto;
import prime.prime.domain.season.models.UserSeasonDto;
import prime.prime.domain.season.service.SeasonServiceImpl;
import prime.prime.domain.technology.entity.Technology;
import prime.prime.domain.user.entity.User;
import prime.prime.domain.user.models.UserCreateDto;
import prime.prime.domain.user.models.UserReturnDto;
import prime.prime.domain.user.models.UserUpdateDto;
import prime.prime.domain.user.service.UserServiceImpl;
import prime.prime.infrastructure.exception.LastAdminException;
import prime.prime.infrastructure.exception.NoTechnologyException;
import prime.prime.infrastructure.security.AuthorizationService;
import prime.prime.infrastructure.security.ProgresoUserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthorizationServiceTest {

    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;
    @InjectMocks
    AuthorizationService authorizationService;
    @Mock
    private UserServiceImpl userService;
    @Mock
    private EventRequestServiceImpl eventRequestService;
    @Mock
    private EventServiceImpl eventService;
    @Mock
    private SeasonServiceImpl seasonService;
    @Mock
    private AbsenceTrackingService absenceTrackingService;
    private static ProgresoUserDetails adminUserDetails;
    private static ProgresoUserDetails mentorUserDetails;
    private static ProgresoUserDetails internUserDetails;
    private static User intern;
    private static SeasonResponseDto seasonResponseDto;
    private static AbsenceResponseDto absenceResponseDtoSeason1;
    private static AbsenceResponseDto absenceResponseDtoSeason2;

    @BeforeEach
    void setUp() {
        List<GrantedAuthority> adminAuthorities = new ArrayList<>();
        adminAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        adminUserDetails = new ProgresoUserDetails(1L, 1L,
            "admin@progreso.com",
            "$2a$10$rnyVHMYp60f64RE1fwe7BO2fcUeZj2qsMG4SHyzqA0cBZtvAPXOAS",
            adminAuthorities);

        List<GrantedAuthority> mentorAuthorities = new ArrayList<>();
        mentorAuthorities.add(new SimpleGrantedAuthority("ROLE_MENTOR"));
        mentorUserDetails = new ProgresoUserDetails(2L, 2L,
            "mentor@progreso.com",
            "$2a$10$rnyVHMYp60f64RE1fwe7BO2fcUeZj2qsMG4SHyzqA0cBZtvAPXOAS",
            mentorAuthorities);

        List<GrantedAuthority> internAuthorities = new ArrayList<>();
        mentorAuthorities.add(new SimpleGrantedAuthority("ROLE_INTERN"));
        internUserDetails = new ProgresoUserDetails(3L, 3L,
                "intern@progreso.com",
                "$2a$10$rnyVHMYp60f64RE1fwe7BO2fcUeZj2qsMG4SHyzqA0cBZtvAPXOAS",
                internAuthorities);

        HashSet<Technology> technologies = new HashSet<>();
        technologies.add(new Technology(1L, "Java"));

        Account mentorAccount = new Account();
        mentorAccount.setEmail("mentor@example.com");
        mentorAccount.setRole(Role.MENTOR);
        User mentor = new User();
        mentor.setId(2L);
        mentor.setAccount(mentorAccount);
        Season season = new Season();
        season.setId(1L);
        season.setOwner(mentor);
        HashSet<Season> seasons = new HashSet<>();
        seasons.add(season);

        Account internAccount = new Account();
        internAccount.setEmail("mia@example.com");
        internAccount.setRole(Role.INTERN);

        intern = new User();
        intern.setId(3L);
        intern.setAccount(internAccount);
        intern.setLocation("Belgrade");
        intern.setFullName("Mia Johnson");
        intern.setDateOfBirth(LocalDate.of(1999, 1, 1));
        intern.setPhoneNumber("0125987456");
        intern.setTechnologies(technologies);
        intern.setSeasons(seasons);

        UserSeasonDto userSeasonDto = new UserSeasonDto(1L, "season 1");

        AccountReturnDto mentorAccountReturn = new AccountReturnDto(mentorAccount.getId(),
            mentorAccount.getEmail(), mentorAccount.getRole(), AccountStatus.ACTIVE);
        UserReturnDto mentorReturnDto = new UserReturnDto(mentor.getId(), "Mentor",
            LocalDate.of(2000, 10, 10),
            "223644", "Location", new HashSet<>(), "",
                mentorAccountReturn, null, Set.of(userSeasonDto));

        seasonResponseDto = new SeasonResponseDto(1L,
            "season 1", 6, SeasonDurationType.MONTHS,
            LocalDate.of(2022, 11, 14),
            LocalDate.of(2023, 6, 12),
            new ArrayList<>(), new HashSet<>(), new HashSet<>(), mentorReturnDto);

        absenceResponseDtoSeason1 = new AbsenceResponseDto(1L, "Absence", "Absence request",
                "PENDING", 3L, "SICK_LEAVE", LocalDateTime.of(2023, 11, 14, 12,30),
                LocalDateTime.of(2023, 11, 15, 12,30), 1L);

        absenceResponseDtoSeason2 = new AbsenceResponseDto(1L, "Absence", "Absence request",
                "PENDING", 3L, "SICK_LEAVE", LocalDateTime.of(2023, 11, 14, 12,30),
                LocalDateTime.of(2023, 11, 15, 12,30), 2L);
    }

    @Test
    void isAllowedToCreate_ValidUserCreateDtoIntern_Successful() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(adminUserDetails);

        HashSet<String> technologies = new HashSet<>();
        technologies.add("Java");
        technologies.add("Flutter");

        UserCreateDto userCreateDto = new UserCreateDto("Alicia Kelly",
            LocalDate.of(2000, 1, 1), "840914879",
            "Belgrade", new AccountCreateDto("tikove6468@v2ssr.com", "INTERN"),
            technologies);

        boolean isAllowed = authorizationService.isAllowedToCreate(userCreateDto);

        assertThat(isAllowed).isEqualTo(true);
    }

    @Test
    void isAllowedToCreate_ValidUserCreateDtoAdmin_Successful() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(adminUserDetails);

        HashSet<String> technologies = new HashSet<>();

        UserCreateDto userCreateDto = new UserCreateDto("Tom Smith",
            LocalDate.of(2000, 1, 1), "111114879",
            "Belgrade", new AccountCreateDto("tomsmith123@v2ssr.com", "ADMIN"),
            technologies);

        boolean isAllowed = authorizationService.isAllowedToCreate(userCreateDto);

        assertThat(isAllowed).isEqualTo(true);
    }

    @Test
    void isAllowedToCreate_MissingTechnologies_ThrowsNoTechnologyException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(adminUserDetails);

        HashSet<String> technologies = new HashSet<>();
        UserCreateDto userCreateDto = new UserCreateDto("Alicia Kelly",
            LocalDate.of(2000, 1, 1), "840914879",
            "Belgrade", new AccountCreateDto("tikove6468@v2ssr.com", "INTERN"),
            technologies);

        assertThrows(NoTechnologyException.class,
            () -> authorizationService.isAllowedToCreate(userCreateDto));
    }

    @Test
    void isAllowedToUpdate_MissingTechnologies_ThrowsNoTechnologyException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(adminUserDetails);

        when(userService.getEntityById(3L)).thenReturn(intern);

        UserUpdateDto userUpdateDto = new UserUpdateDto("Mia Johnson",
            LocalDate.of(1999, 1, 1), "0125987456",
            "Belgrade", new AccountUpdateDto("MENTOR"), new HashSet<>(), null);

        assertThrows(NoTechnologyException.class,
            () -> authorizationService.isAllowedToUpdate(3L, userUpdateDto));
    }

    @Test
    void isAllowedToUpdate_ValidUserUpdateDtoMentor_Successful() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(mentorUserDetails);

        when(userService.getEntityById(3L)).thenReturn(intern);

        HashSet<String> newTechnologies = new HashSet<>();
        newTechnologies.add("Flutter");

        UserUpdateDto userUpdateDto = new UserUpdateDto("Mia Johnson",
            LocalDate.of(1999, 1, 1), "0125987456",
            "Belgrade", new AccountUpdateDto("MENTOR"), newTechnologies, null);

        boolean isAllowed = authorizationService.isAllowedToUpdate(3L, userUpdateDto);

        assertThat(isAllowed).isEqualTo(true);
    }

    @Test
    void isAllowedToUpdate_ValidUserUpdateDtoAdmin_Successful() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(adminUserDetails);

        when(userService.getEntityById(3L)).thenReturn(intern);

        UserUpdateDto userUpdateDto = new UserUpdateDto("Mia Johnson",
            LocalDate.of(1999, 1, 1), "0125987456",
            "Belgrade", new AccountUpdateDto("ADMIN"), new HashSet<>(), null);

        boolean isAllowed = authorizationService.isAllowedToUpdate(3L, userUpdateDto);

        assertThat(isAllowed).isEqualTo(true);
    }

    @Test
    void isAllowedToUpdate_NotAdminAndMissingTechnologies_False() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(mentorUserDetails);

        when(userService.getEntityById(3L)).thenReturn(intern);

        UserUpdateDto userUpdateDto = new UserUpdateDto("Mia Johnson",
            LocalDate.of(1999, 1, 1), "0125987456",
            "Belgrade", new AccountUpdateDto("MENTOR"), new HashSet<>(), null);

        boolean isAllowed = authorizationService.isAllowedToUpdate(3L, userUpdateDto);

        assertThat(isAllowed).isEqualTo(false);
    }

    @Test
    void isAllowedToDelete_True() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(adminUserDetails);

        when(userService.getEntityById(3L)).thenReturn(intern);

        boolean isAllowedToDelete = authorizationService.isAllowedToDelete(3L);

        assertThat(isAllowedToDelete).isEqualTo(true);
    }

    @Test
    void isAdminOrOwner_Admin_True() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(adminUserDetails);

        EventResponseWithAttendeesDto responseWithAttendeesDto = new EventResponseWithAttendeesDto
            (1L, "title", "description",
                LocalDateTime.of(LocalDate.of(2023, 9, 9), LocalTime.of(10, 0)),
                30L, LocalDateTime.of(2023, 9, 9, 10, 30), 1L, 1L,
                Set.of(new AttendeesDto(2L, "John Smith", "johnsmith@progreso.com", false)));

        when(eventService.getById(1L)).thenReturn(responseWithAttendeesDto);

        boolean isAdminOrOwner = authorizationService.isAdminOrOwner(1L);

        assertThat(isAdminOrOwner).isEqualTo(true);
    }

    @Test
    void isAdminOrOwner_NotOwnerOrAdmin_False() {
        List<GrantedAuthority> mentorAuthorities = new ArrayList<>();
        mentorAuthorities.add(new SimpleGrantedAuthority("ROLE_MENTOR"));
        mentorUserDetails = new ProgresoUserDetails(3L, 3L,
            "mentor2@progreso.com",
            "$2a$10$rnyVHMYp60f64RE1fwe7BO2fcUeZj2qsMG4SHyzqA0cBZtvAPXOAS",
            mentorAuthorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(mentorUserDetails);

        EventResponseWithAttendeesDto responseWithAttendeesDto = new EventResponseWithAttendeesDto
            (1L, "title", "description",
                LocalDateTime.of(LocalDate.of(2023, 9, 9), LocalTime.of(10, 0)),
                30L, LocalDateTime.of(2023, 9, 9, 10, 30), 1L, 1L,
                Set.of(new AttendeesDto(2L, "John Smith", "johnsmith@progreso.com", false)));

        when(eventService.getById(1L)).thenReturn(responseWithAttendeesDto);
        when(seasonService.getById(1L)).thenReturn(seasonResponseDto);

        boolean isAdminOrOwner = authorizationService.isAdminOrOwner(1L);

        assertThat(isAdminOrOwner).isEqualTo(false);
    }

    @Test
    void isAllowedToDeleteUserImage_Owner_True() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(mentorUserDetails);

        boolean isAllowed = authorizationService.isAllowedToDeleteUserImage(2L);

        assertThat(isAllowed).isEqualTo(true);
    }

    @Test
    void isAllowedToDeleteUserImage_NotOwner_False() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(mentorUserDetails);

        boolean isAllowed = authorizationService.isAllowedToDeleteUserImage(1L);

        assertThat(isAllowed).isEqualTo(false);
    }

    @Test
    void isOwnerOfRequest_Owner_True() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(mentorUserDetails);

        EventRequestReturnDto eventRequestReturnDto = new EventRequestReturnDto(1L,
            "title", "description", "status", 2L, 1L);
        when(eventRequestService.getById(1L)).thenReturn(eventRequestReturnDto);

        boolean isOwner = authorizationService.isOwnerOfRequest(1L);

        assertThat(isOwner).isEqualTo(true);
    }

    @Test
    void isOwnerOfRequest_NotOwner_False() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(mentorUserDetails);

        EventRequestReturnDto eventRequestReturnDto = new EventRequestReturnDto(1L,
            "title", "description", "status", 1L, 1L);
        when(eventRequestService.getById(1L)).thenReturn(eventRequestReturnDto);

        boolean isOwner = authorizationService.isOwnerOfRequest(1L);

        assertThat(isOwner).isEqualTo(false);
    }

    @Test
    void isAllowedToDeleteAdmin_ThrowsLastAdminException() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        ProgresoUserDetails userDetails = new ProgresoUserDetails(1L, 1L, "admin@xprmn.xyz",
            "$2a$10$rnyVHMYp60f64RE1fwe7BO2fcUeZj2qsMG4SHyzqA0cBZtvAPXOAS",
            authorities);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        Account account = new Account();
        account.setRole(Role.ADMIN);
        account.setEmail("admin@xprmnt.xyz");

        User user = new User();
        user.setId(1L);
        user.setAccount(account);
        user.setLocation("Sofia");
        user.setFullName("Administrator");
        user.setDateOfBirth(LocalDate.of(1999, 1, 1));
        user.setPhoneNumber("00000000000");

        when(userService.getEntityById(anyLong())).thenReturn(user);
        assertThrows(LastAdminException.class, () ->
            authorizationService.isAllowedToDelete(user.getId()));
    }

    @Test
    void isOwnerOfSeason_Owner_True() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(mentorUserDetails);

        when(seasonService.getById(1L)).thenReturn(seasonResponseDto);

        boolean isOwner = authorizationService.isOwnerOfSeason(1L);

        assertThat(isOwner).isEqualTo(true);
    }

    @Test
    void isAllowedToChangeEventRequest_Allowed_True() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(mentorUserDetails);

        when(seasonService.getById(1L)).thenReturn(seasonResponseDto);

        EventRequestReturnDto eventRequestReturnDto = new EventRequestReturnDto(1L,
            "title", "description", "REQUESTED", 1L, 1L);
        when(eventRequestService.getById(1L)).thenReturn(eventRequestReturnDto);

        boolean isAllowed = authorizationService.isAllowedToChangeEventRequest(1L);

        assertThat(isAllowed).isEqualTo(true);
    }

    @Test
    void canAccessAbsenceRequest_True() {
        when(absenceTrackingService.getById(1L)).thenReturn(absenceResponseDtoSeason1);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(internUserDetails);
        when(userService.getEntityById(3L)).thenReturn(intern);

        boolean result = authorizationService.canAccessAbsenceRequest(1L);

        assertThat(result).isEqualTo(true);
    }

    @Test
    void canAccessAbsenceRequest_False() {
        when(absenceTrackingService.getById(1L)).thenReturn(absenceResponseDtoSeason2);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(internUserDetails);
        when(userService.getEntityById(3L)).thenReturn(intern);

        boolean result = authorizationService.canAccessAbsenceRequest(1L);

        assertThat(result).isEqualTo(false);
    }

    @Test
    void canAccessEventRequest_True() {
        EventRequestReturnDto eventRequestReturnDto = new EventRequestReturnDto(1L,
                "title", "description", "REQUESTED", 1L, 1L);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(internUserDetails);

        when(userService.getEntityById(anyLong())).thenReturn(intern);
        when(eventRequestService.getById(anyLong())).thenReturn(eventRequestReturnDto);
        when(seasonService.getById(1L)).thenReturn(seasonResponseDto);

        boolean result = authorizationService.canAccessEventRequest(1L);

        assertThat(result).isEqualTo(true);
    }
}
