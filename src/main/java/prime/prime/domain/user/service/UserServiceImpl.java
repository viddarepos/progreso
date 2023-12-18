package prime.prime.domain.user.service;

import jakarta.servlet.http.HttpServletRequest;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import prime.prime.domain.account.entity.Account;
import prime.prime.domain.account.entity.AccountStatus;
import prime.prime.domain.account.models.AccountEmailDto;
import prime.prime.domain.account.models.ChangePasswordDto;
import prime.prime.domain.account.service.AccountServiceImpl;
import prime.prime.domain.role.Role;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.technology.mapper.TechnologyNameToTechnologyEntity;
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
import prime.prime.infrastructure.password.generator.RandomPasswordGenerator;
import prime.prime.infrastructure.security.JwtUtil;
import prime.prime.infrastructure.security.ProgresoUserDetails;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final String IMAGE_NAME = "ProfilePicture%s.png";
    private static final String UPLOAD_DIR = "uploads/profile_pictures";
    private static final String IMAGE_FOLDER = "/profile_pictures/";
    private static final String TEMPLATE = "registration_email";
    @Value("${progreso.email.admin}")
    private String adminEmail;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private
    final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TechnologyNameToTechnologyEntity technologyNameToTechnology;
    private final AccountServiceImpl accountService;
    private final JwtUtil jwtUtil;
    private final ActivationReminderJob activationReminderJob;
    private final EmailSendingJob emailSendingJob;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper,
                           TechnologyNameToTechnologyEntity technologyNameToTechnology,
                           AccountServiceImpl accountService, JwtUtil jwtUtil,
                           ActivationReminderJob activationReminderJob, EmailSendingJob emailSendingJob) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.technologyNameToTechnology = technologyNameToTechnology;
        this.accountService = accountService;
        this.jwtUtil = jwtUtil;
        this.activationReminderJob = activationReminderJob;
        this.emailSendingJob = emailSendingJob;
    }

    @Override
    public User getEntityById(Long id) {
        return userRepository.findById(id).orElseThrow(
            () -> new NotFoundException(User.class.getSimpleName(), "id", id.toString())
        );
    }

    @Override
    public UserReturnDto create(UserCreateDto userCreateDto) {
        validateEmail(userCreateDto);

        String accPassword = RandomPasswordGenerator.getRandomPassword();

        User user = createUser(userCreateDto, accPassword);

        scheduleMailSending(accPassword, user);

        notifyUserAboutAccountActivation(user);

        return userMapper.userToReturnDto(userRepository.save(user));
    }

    @Override
    public UserReturnDto getById(Long id) {
        User user = findUserById(id);
        return userMapper.userToReturnDto(user);
    }

    @Override
    public Page<UserReturnDto> getAll(SearchUserDto searchUserDto, Pageable pageable, ProgresoUserDetails userDetails) {
        return userRepository.findAll(new UserSpecification(searchUserDto,userDetails), pageable)
            .map(userMapper::userToReturnDto);
    }

    @Override
    public UserReturnDto update(Long id, UserUpdateDto userUpdateDto, MultipartFile multipartFile)
        throws IOException {
        existsUserById(id);
        User user = findUserById(id);

        validateAdminAccount(user);
        if (userUpdateDto.technologies() != null) {
            user.setTechnologies(
                technologyNameToTechnology.toTechnologyEntity(userUpdateDto.technologies()));
        }

        if (multipartFile != null) {
            user.setImagePath(uploadImage(multipartFile, id));
        }
        userMapper.update(userUpdateDto, user);
        return userMapper.userToReturnDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public AccountEmailDto delete(Long id) throws IOException {
        User user = getEntityById(id);

        validateOwner(user);
        validateAdminAccount(user);
        deleteImageFromFolder(id);

        userRepository.deleteById(id);
        return new AccountEmailDto(user.getAccount().getEmail());
    }

    @Override
    public AccountEmailDto changePassword(Long id, ChangePasswordDto changePasswordDto,
        HttpServletRequest httpServletRequest) {

        String email = jwtUtil.getUserEmailFromHeader(httpServletRequest);
        UserProjection userByAccountEmail = userRepository.findUserByAccountEmail(email)
            .orElseThrow(() -> new NotFoundException(Account.class.getSimpleName(), "email",
                email));

        if (!Objects.equals(userByAccountEmail.getId(), id)) {
            throw new ChangePasswordAccessException();
        }
        if (passwordEncoder.matches(changePasswordDto.oldPassword(),
            userByAccountEmail.getAccountPassword())) {
            accountService.update(userByAccountEmail.getAccountId(),
                passwordEncoder.encode(changePasswordDto.newPassword()));
        } else {
            throw new InvalidOldPasswordException();
        }

        return new AccountEmailDto(email);

    }

    @Override
    public UserProjection findUserByAccountEmail(String email) {
        return userRepository.findUserByAccountEmail(email)
            .orElseThrow(() -> new NotFoundException(Account.class.getSimpleName(), "email",
                email));
    }

    private String uploadImage(MultipartFile multipartFile, Long id) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(String.format(IMAGE_NAME, id));
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            throw new IOException("Could not save image file: " + String.format(IMAGE_NAME, id),
                ioe);
        }

        return IMAGE_FOLDER + String.format(IMAGE_NAME, id);
    }

    @Override
    public void deleteUserImage(Long id) throws IOException {
        User user = getEntityById(id);

        if (deleteImageFromFolder(id)) {
            user.setImagePath(null);
            userRepository.save(user);
        }
    }

    @Override
    public AccountEmailDto archive(Long id) {
        User user = findUserById(id);

        if (user.getAccount().getEmail().equals(adminEmail)) {
            throw new AccessDeniedException("The default admin user account can not be archived!");
        }

        validateOwner(user);

        accountService.archive(user.getAccount().getId());
        return new AccountEmailDto(user.getAccount().getEmail());
    }

    @Override
    public Set<User> getAllAdmins() {
        return userRepository.findAll()
            .stream()
            .filter(user -> user.getAccount().getRole().equals(Role.ADMIN))
            .collect(Collectors.toSet());

    }

    @Override
    public Set<User> getAllById(Set<Long> users) {
        var actualUsers = userRepository.findAllById(users);

        if (users.size() == actualUsers.size()) {
            return new HashSet<>(actualUsers);
        }

        Set<Long> actualUsersId = actualUsers
            .stream()
            .map(User::getId)
            .collect(Collectors.toSet());

        var notFoundIds = users
            .stream()
            .filter(id -> !actualUsersId.contains(id))
            .toList();

        throw new NotFoundException("Users", "id", notFoundIds.toString());
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByAccountEmail(email)
            .orElseThrow(() -> new NotFoundException(Account.class.getSimpleName(), "email",
                email));
    }

    @Override
    public boolean checkIfUserIsNotAssignedToSeason(User user, Season season) {
        return user.getSeasons().stream()
            .noneMatch(userSeason -> userSeason.getId().equals(season.getId()));
    }

    private void validateOwner(User user) {
        if (user.getSeasonsOwner() != null && !user.getSeasonsOwner().isEmpty()) {
            throw new OwnerException("User is owner of the season, cannot be deleted.");
        }
    }

    private void validateAdminAccount(User user) {
        if (user.getAccount().getEmail().equals("admin@xprmnt.xyz") && user.getAccount().getRole()
            .equals(Role.ADMIN)) {
            throw new LastAdminException("Cannot update or delete the application administrator");
        }
    }

    private boolean deleteImageFromFolder(Long id) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        Path filePath = uploadPath.resolve(String.format(IMAGE_NAME, id));
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            return true;
        }
        return false;
    }

    private void existsUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException(User.class.getSimpleName(), "id", id.toString());
        }
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(
                () -> new NotFoundException(User.class.getSimpleName(), "id", id.toString()));
    }

    private void scheduleMailSending(String accPassword, User user) {
        Map<String,String> content = Map.of("password",accPassword,"fullName",user.getFullName(),"template",TEMPLATE);
        emailSendingJob.scheduleEmailJob(user.getAccount().getEmail(),"Account created",content);
    }

    private void validateEmail(UserCreateDto userCreateDto) {
        if (userRepository.existsUserByAccountEmail(userCreateDto.account().email())) {
            throw new DuplicateException(Account.class.getSimpleName(), "email",
                userCreateDto.account().email());
        }
    }

    private User createUser(UserCreateDto userCreateDto, String accPassword) {
        User user = userMapper.createDtoToUser(userCreateDto);
        Account account = user.getAccount();

        account.setPassword(passwordEncoder.encode(accPassword));
        account.setStatus(AccountStatus.INVITED);
        user.setAccount(account);

        return user;
    }

    private void notifyUserAboutAccountActivation(User user) {
        LocalDate creationDate = LocalDate.now();
        JobDetail jobDetail = activationReminderJob.buildJobDetail(user, creationDate);
        Trigger trigger = activationReminderJob.buildJobTrigger(jobDetail,
            creationDate.plusDays(7));
        activationReminderJob.scheduleJob(jobDetail, trigger);
    }
}
