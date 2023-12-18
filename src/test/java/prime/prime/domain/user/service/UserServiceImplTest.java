package prime.prime.domain.user.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.quartz.Scheduler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import prime.prime.domain.account.entity.Account;
import prime.prime.domain.account.entity.AccountStatus;
import prime.prime.domain.account.models.*;
import prime.prime.domain.account.service.AccountServiceImpl;
import prime.prime.domain.event.entity.Event;
import prime.prime.domain.event.repository.EventRepository;
import prime.prime.domain.eventattendees.entity.EventAttendee;
import prime.prime.domain.role.Role;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.season.models.UserSeasonDto;
import prime.prime.domain.technology.entity.Technology;
import prime.prime.domain.technology.mapper.TechnologyNameToTechnologyEntity;
import prime.prime.domain.technology.models.TechnologyReturnDto;
import prime.prime.domain.user.entity.IntegrationType;
import prime.prime.domain.user.entity.User;
import prime.prime.domain.user.mappers.UserMapper;
import prime.prime.domain.user.models.SearchUserDto;
import prime.prime.domain.user.models.UserCreateDto;
import prime.prime.domain.user.models.UserReturnDto;
import prime.prime.domain.user.models.UserUpdateDto;
import prime.prime.domain.user.repository.Projection.UserProjection;
import prime.prime.domain.user.repository.UserRepository;
import prime.prime.domain.user.repository.UserSpecification;
import prime.prime.infrastructure.exception.*;
import prime.prime.infrastructure.jobs.ActivationReminderJob;
import prime.prime.infrastructure.jobs.EmailSendingJob;
import prime.prime.infrastructure.security.JwtUtil;
import prime.prime.infrastructure.security.ProgresoUserDetails;
import prime.prime.infrastructure.utility.PropertiesExtractor;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    private static AccountCreateDto accountCreateDto;
    private static User user;
    private static UserCreateDto userCreateDto;
    private static UserUpdateDto userUpdateDto;
    private static UserReturnDto userReturnDto;
    private static Event event;
    private static MockMultipartFile multipartFile;
    private static UserProjection userProjection;
    private static String adminEmail;
    private static Season season;
    private static ProgresoUserDetails userDetails;
    private static User userIntern;
    private static UserReturnDto userInternReturnDto;

    @Mock
    private static TechnologyNameToTechnologyEntity technologyNameToTechnology;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailSendingJob emailSendingJob;
    @Mock
    private UserMapper userMapper;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private AccountServiceImpl accountService;
    @Mock
    private ActivationReminderJob activationReminderJob;
    @Mock
    private Scheduler scheduler;
    @Mock
    private static JwtUtil jwtUtil;
    @InjectMocks
    private UserServiceImpl userService;
    private static MockHttpServletRequest request;
    private static final ProjectionFactory factory = new SpelAwareProxyProjectionFactory();

    @BeforeAll
    static void setUp() {
        multipartFile = new MockMultipartFile("ProfilePicture1.png",
            "ProfilePicture1.png".getBytes());

        adminEmail = PropertiesExtractor.getProperty("progreso.email.admin");

        Account account = new Account();
        account.setId(1L);
        account.setEmail(adminEmail);
        account.setPassword("Testing_16");
        account.setRole(Role.ADMIN);

        accountCreateDto = new AccountCreateDto(
            account.getEmail(),
            account.getRole().name());

        AccountReturnDto accountReturnDto = new AccountReturnDto(
            account.getId(),
            account.getEmail(),
            account.getRole(),
            account.getStatus());

        AccountUpdateDto accountUpdateDto = new AccountUpdateDto(
            account.getRole().name());

        Technology technology = new Technology(1L, "Java");

        Set<Technology> technologies = new HashSet<>();
        technologies.add(technology);

        Set<String> technologiesString = new HashSet<>();
        technologiesString.add("Java");

        Set<TechnologyReturnDto> technologyCreateDtoList = new HashSet<>();
        Set<Season> seasons = new HashSet<>();
        season = new Season();
        season.setId(1L);
        season.setStartDate(LocalDate.now().plusDays(1));
        season.setEndDate(LocalDate.now().plusMonths(6));
        season.setName("Season 1");
        seasons.add(season);

        user = new User();
        user.setId(1L);
        user.setFullName("Testing");
        user.setDateOfBirth(LocalDate.parse("2022-09-09"));
        user.setLocation("Location");
        user.setPhoneNumber("123456789");
        user.setAccount(account);
        user.setTechnologies(technologies);
        user.setSeasonsOwner(new HashSet<>());
        user.setSeasons(seasons);
        userDetails = new ProgresoUserDetails(account.getId(), user.getId(), account.getEmail()
                , account.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + account.getRole().name())));

        Account accountIntern = new Account();
        accountIntern.setId(2L);
        accountIntern.setEmail("test@gmail.com");
        accountIntern.setPassword("Testing_16");
        accountIntern.setRole(Role.INTERN);

        userIntern = new User();
        userIntern.setId(2L);
        userIntern.setFullName("Intern user");
        userIntern.setDateOfBirth(LocalDate.parse("2002-10-11"));
        userIntern.setLocation("Location");
        userIntern.setPhoneNumber("123456789");
        userIntern.setAccount(accountIntern);
        userIntern.setTechnologies(Set.of(new Technology(2L, "Flutter")));
        userIntern.setSeasonsOwner(new HashSet<>());
        userIntern.setSeasons(seasons);

        Set<User> attendees = new HashSet<>();
        attendees.add(user);

        event = new Event();
        event.setId(1L);
        event.setTitle("Test");
        event.setDescription("");
        event.setStartTime(LocalDateTime.now());
        event.setDuration(Duration.ofMinutes(10));
        event.setCreator(user);

        Set<EventAttendee> eventAttendees = new HashSet<>();
        EventAttendee eventAttendee = new EventAttendee();
        eventAttendee.setEvent(event);
        eventAttendee.setUser(user);
        eventAttendee.setRequired(false);
        eventAttendees.add(eventAttendee);

        Set<Event> events = new HashSet<>();
        events.add(event);
        event.setEventAttendees(eventAttendees);
        user.setEvent(events);

        userCreateDto = new UserCreateDto(
            user.getFullName(),
            user.getDateOfBirth(),
            user.getPhoneNumber(),
            user.getLocation(),
            accountCreateDto,
            technologiesString
        );
        UserSeasonDto userSeasonDto = new UserSeasonDto(1L,"Season 1");
        userReturnDto = new UserReturnDto(
            user.getId(),
            user.getFullName(),
            user.getDateOfBirth(),
            user.getPhoneNumber(),
            user.getLocation(),
            Set.of(new TechnologyReturnDto(1L,"Java")),
            null,
            accountReturnDto,
            Set.of(IntegrationType.GOOGLE),
                Set.of(userSeasonDto)
        );

        userInternReturnDto = new UserReturnDto(userIntern.getId(), userIntern.getFullName(),
                userIntern.getDateOfBirth(), userIntern.getPhoneNumber(), userIntern.getLocation(), null,
                userIntern.getImagePath(), new AccountReturnDto(accountIntern.getId(), accountIntern.getEmail(),
                accountIntern.getRole(), accountIntern.getStatus()), userIntern.getIntegrations(), Set.of(userSeasonDto));

        userUpdateDto = new UserUpdateDto(
            user.getFullName(),
            user.getDateOfBirth(),
            user.getPhoneNumber(),
            user.getLocation(),
            accountUpdateDto,
            technologiesString,
            Set.of("GOOGLE"));

        userProjection = factory.createProjection(UserProjection.class);
        userProjection.setAccountId(account.getId());
        userProjection.setId(1L);
        userProjection.setAccountEmail(account.getEmail());
        userProjection.setAccountPassword(
            "$2a$10$rnyVHMYp60f64RE1fwe7BO2fcUeZj2qsMG4SHyzqA0cBZtvAPXOAS");
        userProjection.setAccountStatus(AccountStatus.ACTIVE);
        userProjection.setAccountRole("ROLE_ADMIN");

        request = new MockHttpServletRequest();
        request.addHeader("Authorization",
            "Bearer eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEsInN1YiI6ImFkbWluQHhwcm1udC54eXoiLC");
    }

    @Test
    void create_ValidUserCreateDto_Successful() {
        when(userRepository.existsUserByAccountEmail(accountCreateDto.email())).thenReturn(false);
        when(userMapper.createDtoToUser(userCreateDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        emailSendingJob.scheduleEmailJob(accountCreateDto.email(), "Account created", Map.of("fullName", "Test",
                "password", "Test123+"));
        when(userMapper.userToReturnDto(user)).thenReturn(userReturnDto);

        UserReturnDto userReturnDtoResult = userService.create(userCreateDto);

        assertThat(userReturnDtoResult).usingRecursiveComparison().isEqualTo(userReturnDto);
    }

    @Test
    void create_EmailAlreadyExists_ThrowsDuplicateException() {
        when(userRepository.existsUserByAccountEmail(accountCreateDto.email())).thenReturn(true);

        assertThrows(DuplicateException.class, () -> userService.create(userCreateDto));
    }

    @Test
    void getById_ValidId_Successful() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userMapper.userToReturnDto(user)).thenReturn(userReturnDto);

        UserReturnDto userReturnDtoResult = userService.getById(user.getId());

        assertThat(userReturnDtoResult).usingRecursiveComparison().isEqualTo(userReturnDto);
    }

    @Test
    void getById_InvalidId_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class, () -> userService.getById(0L));
    }

    @Test
    void getAll_Valid_Successful() {
        List<User> list = List.of(user);
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> userReturnPage = new PageImpl<>(list, pageable, list.size());
        SearchUserDto searchUserDto = new SearchUserDto("a", "a", "a", "a","Season 1","Java");

        when(userRepository.findAll(any(UserSpecification.class), any(Pageable.class))).thenReturn(
            userReturnPage);
        when(userMapper.userToReturnDto(user)).thenReturn(userReturnDto);

        Page<UserReturnDto> result = userService.getAll(searchUserDto, pageable, userDetails);

        UserReturnDto returnDto = userMapper.userToReturnDto(userReturnPage.getContent().get(0));
        UserReturnDto returnResultDto = result.getContent().get(0);

        assertEquals(1, result.getTotalElements());
        assertThat(returnResultDto).usingRecursiveComparison().isEqualTo(returnDto);
    }

    @Test
    void getAll_GroupByLocation_Successful() {
        List<User> list = List.of(user,userIntern);
        Pageable pageable = PageRequest.of(0, 2);
        Page<User> userReturnPage = new PageImpl<>(list, pageable, list.size());
        SearchUserDto searchUserDto = new SearchUserDto(null, "Location", null, null,null,null);

        when(userRepository.findAll(any(UserSpecification.class), any(Pageable.class))).thenReturn(
                userReturnPage);
        when(userMapper.userToReturnDto(user)).thenReturn(userReturnDto);
        when(userMapper.userToReturnDto(userIntern)).thenReturn(userInternReturnDto);

        Page<UserReturnDto> result = userService.getAll(searchUserDto, pageable, userDetails);

        assertNotNull(result.getContent());
        assertEquals(2, result.getTotalElements());
        assertEquals("Location",result.getContent().get(0).location());
        assertEquals("Location",result.getContent().get(1).location());
    }

    @Test
    void getAll_GroupBySeason_Successful() {
        List<User> list = List.of(user,userIntern);
        Pageable pageable = PageRequest.of(0, 2);
        Page<User> userReturnPage = new PageImpl<>(list, pageable, list.size());
        SearchUserDto searchUserDto = new SearchUserDto(null, null, null, null,"Season 1",null);

        when(userRepository.findAll(any(UserSpecification.class), any(Pageable.class))).thenReturn(
                userReturnPage);
        when(userMapper.userToReturnDto(user)).thenReturn(userReturnDto);
        when(userMapper.userToReturnDto(userIntern)).thenReturn(userInternReturnDto);

        Page<UserReturnDto> result = userService.getAll(searchUserDto, pageable, userDetails);

        assertNotNull(result.getContent());
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().get(0).seasons().stream().anyMatch(s-> s.name().equals("Season 1")));
        assertTrue(result.getContent().get(1).seasons().stream().anyMatch(s-> s.name().equals("Season 1")));
    }

    @Test
    void getAll_GroupByTechnology_Successful() {
        List<User> list = List.of(user);
        Pageable pageable = PageRequest.of(0, 1);
        Page<User> userReturnPage = new PageImpl<>(list, pageable, list.size());
        SearchUserDto searchUserDto = new SearchUserDto(null, null, null, null,null,"Java");

        when(userRepository.findAll(any(UserSpecification.class), any(Pageable.class))).thenReturn(
                userReturnPage);
        when(userMapper.userToReturnDto(user)).thenReturn(userReturnDto);

        Page<UserReturnDto> result = userService.getAll(searchUserDto, pageable, userDetails);

        assertNotNull(result.getContent());
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).technologies().stream().anyMatch(s-> s.name().equals("Java")));
    }

    @Test
    void getAll_EmptyPage_Successful() {
        Pageable pageable = PageRequest.of(0, 20);
        SearchUserDto searchUserDto = new SearchUserDto("a", "a", "a", "a","Season 1","Java");

        when(userRepository.findAll(any(UserSpecification.class), any(Pageable.class))).thenReturn(
            Page.empty());

        Page<UserReturnDto> result = userService.getAll(searchUserDto, pageable,userDetails);

        assertEquals(0, result.getTotalElements());
        assertDoesNotThrow(() -> userService.getAll(searchUserDto, pageable,userDetails));
    }

    @Test
    void update_ValidUserUpdateDtoNoImage_Successful() throws IOException {
        when(userRepository.existsById(user.getId())).thenReturn(true);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.userToReturnDto(user)).thenReturn(userReturnDto);
        when(
            technologyNameToTechnology.toTechnologyEntity(userUpdateDto.technologies())).thenReturn(
            user.getTechnologies());

        UserReturnDto userReturnDtoResult = userService.update(user.getId(), userUpdateDto, null);

        var expectedEventAttendee = new EventAttendee(true, event, user);
        assertThat(event.getEventAttendees().contains(expectedEventAttendee));
        assertThat(userReturnDtoResult).usingRecursiveComparison().isEqualTo(userReturnDto);
    }

    @Test
    void update_ValidUserUpdateDtoWithImage_Successful() throws IOException {
        Account account = new Account();
        account.setRole(Role.MENTOR);
        account.setEmail("testing@gmail.com");
        account.setPassword("Testing_16");
        User userWithImage = new User();
        userWithImage.setId(1L);
        userWithImage.setFullName("Testing");
        userWithImage.setDateOfBirth(LocalDate.parse("2022-09-09"));
        userWithImage.setLocation("Location");
        userWithImage.setPhoneNumber("123456789");
        userWithImage.setImagePath("/profile_pictures/" + multipartFile.getName());
        userWithImage.setAccount(account);

        UserReturnDto userReturnDtoWithImage = new UserReturnDto(userWithImage.getId(),
            userWithImage.getFullName(),
            userWithImage.getDateOfBirth(),
            userWithImage.getPhoneNumber(),
            userWithImage.getLocation(),
            userReturnDto.technologies(),
            userWithImage.getImagePath(),
            userReturnDto.account(),
            null,
                null);

        when(userRepository.existsById(userWithImage.getId())).thenReturn(true);
        when(userRepository.findById(userWithImage.getId())).thenReturn(Optional.of(userWithImage));
        when(userRepository.save(userWithImage)).thenReturn(userWithImage);
        when(userMapper.userToReturnDto(userWithImage)).thenReturn(userReturnDtoWithImage);
        when(
            technologyNameToTechnology.toTechnologyEntity(userUpdateDto.technologies())).thenReturn(
            userWithImage.getTechnologies());

        UserReturnDto userReturnDtoResult = userService.update(userWithImage.getId(), userUpdateDto,
            multipartFile);

        assertThat(userReturnDtoResult).usingRecursiveComparison()
            .isEqualTo(userReturnDtoWithImage);
        var expectedEventAttendee = new EventAttendee(false, event, user);
        assertThat(event.getEventAttendees().contains(expectedEventAttendee));
        assertThat(userReturnDtoResult).usingRecursiveComparison()
            .isEqualTo(userReturnDtoWithImage);
    }
    @Test
    void update_UpdateValidAdminCredentials_ThrowsLastAdminException() {
        Account account = new Account();
        account.setRole(Role.ADMIN);
        account.setEmail("admin@xprmnt.xyz");
        account.setPassword("admin");
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setFullName("Administrator");
        adminUser.setDateOfBirth(LocalDate.parse("2022-09-09"));
        adminUser.setLocation("Location");
        adminUser.setPhoneNumber("123456789");
        adminUser.setAccount(account);

        when(userRepository.existsById(adminUser.getId())).thenReturn(true);
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));

        assertThrows(LastAdminException.class,
            () -> userService.update(adminUser.getId(), userUpdateDto,
                multipartFile));
    }

    @Test
    void update_InvalidId_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class,
            () -> userService.update(0L, userUpdateDto, multipartFile));
    }

    @Test
    void delete_ValidId_Successful() throws IOException {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(userMapper.userToReturnDto(user)).thenReturn(userReturnDto);

        AccountEmailDto deleteDto = userService.delete(1L);

        assertEquals(deleteDto.email(), user.getAccount().getEmail());
        assertThat(event.getEventAttendees().isEmpty());
        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_InvalidId_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class, () -> userService.delete(0L));
    }

    @Test
    void getAllById_ValidId_Successful() {
        when(userRepository.findAllById(Set.of(1L))).thenReturn(List.of(user));

        Set<User> users = userService.getAllById(Set.of(1L));

        assertTrue(users.contains(user));
    }

    @Test
    void getAllById_invalidId_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class, () -> userService.getAllById(Set.of(0L)));
    }

    @Test
    void archive_ValidIdAndNotArchivedAccount_Successful() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.ofNullable(user));
        doNothing().when(accountService).archive(user.getAccount().getId());

        userService.archive(user.getId());

        verify(accountService).archive(user.getAccount().getId());
    }

    @Test
    void archive_InvalidId_ThrowsNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.archive(user.getId()));
    }

    @Test
    void archive_InvalidDefaultAdmin_ThrowsException() {
        ReflectionTestUtils.setField(userService, "adminEmail", adminEmail);
        User admin = new User();
        Account account = new Account();
        account.setEmail(adminEmail);
        admin.setAccount(account);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(admin));

        assertThrows(AccessDeniedException.class, () -> userService.archive(user.getId()));
    }

    @Test
    void findActiveUserByAccountEmail_ValidEmail_Successful() {
        when(userRepository.findUserByAccountEmail(user.getAccount().getEmail())).thenReturn(
            Optional.ofNullable(userProjection));

        when(accountService.checkAccountStatus(userProjection.getAccountEmail())).thenReturn(
            userProjection);

        UserProjection userProjection = userService.findUserByAccountEmail(
            user.getAccount().getEmail());
        assertEquals(AccountStatus.ACTIVE, userProjection.getAccountStatus());
    }

    @Test
    void findActiveUserByAccountEmail_InvalidEmail_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class,
            () -> userService.findUserByAccountEmail("test@mail.com"));
    }

    @Test
    void changePassword_ValidChangePasswordDto_Successful() {
        when(jwtUtil.getUserEmailFromHeader(request)).thenReturn("admin@xprmnt.xyz");
        when(userRepository.findUserByAccountEmail("admin@xprmnt.xyz")).thenReturn(
            Optional.of(userProjection));

        AccountEmailDto accountEmailDto = userService.changePassword(1L,
            new ChangePasswordDto("Admin123+", "Admin1234+"),
                request);

        assertEquals("admin@xprmnt.xyz", accountEmailDto.email());
    }

    @Test
    void changePassword_InvalidOldPassword_ThrowsInvalidOldPasswordException() {
        when(jwtUtil.getUserEmailFromHeader(request))
            .thenReturn("admin@xprmnt.xyz");
        when(userRepository.findUserByAccountEmail("admin@xprmnt.xyz")).thenReturn(
            Optional.of(userProjection));

        assertThrows(InvalidOldPasswordException.class,
            () -> userService.changePassword(1L, new ChangePasswordDto("Admin", "Admin1234+"),
                    request));
    }

    @Test
    void changePassword_InvalidId_ThrowsChangePasswordAccessException() {
        when(jwtUtil.getUserEmailFromHeader(request))
            .thenReturn("admin@xprmnt.xyz");

        when(userRepository.findUserByAccountEmail("admin@xprmnt.xyz")).thenReturn(
            Optional.of(userProjection));

        assertThrows(ChangePasswordAccessException.class,
            () -> userService.changePassword(2L, new ChangePasswordDto("Admin", "Admin1234+"),
                    request));
    }

    @Test
    void findByEmail_ValidEmail_Successful() {
        when(userRepository.findByAccountEmail(user.getAccount().getEmail())).thenReturn(
            Optional.ofNullable(user));

        User returnedUser = userService.findByEmail(user.getAccount().getEmail());
        assertEquals(adminEmail, returnedUser.getAccount().getEmail());
    }

    @Test
    void findByEmail_InvalidEmail_ThrowsNotFoundException() {
        assertThrows(NotFoundException.class,
            () -> userService.findUserByAccountEmail("test@mail.com"));
    }
}
