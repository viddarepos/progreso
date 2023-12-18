package prime.prime.domain.season.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import prime.prime.domain.role.Role;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.season.mapper.SeasonMapper;
import prime.prime.domain.season.models.SearchSeasonDto;
import prime.prime.domain.season.models.SeasonCreateDto;
import prime.prime.domain.season.models.SeasonResponseDto;
import prime.prime.domain.season.models.SeasonUpdateDto;
import prime.prime.domain.season.repository.SeasonRepository;
import prime.prime.domain.season.repository.SeasonSpecification;
import prime.prime.domain.season.utility.SeasonUtility;
import prime.prime.domain.technology.mapper.TechnologyNameToTechnologyEntity;
import prime.prime.domain.user.entity.User;
import prime.prime.domain.user.service.UserService;
import prime.prime.infrastructure.exception.NotFoundException;
import prime.prime.infrastructure.exception.OwnerException;
import prime.prime.infrastructure.exception.SeasonException;
import prime.prime.infrastructure.jobs.EmailSendingJob;
import prime.prime.infrastructure.security.ProgresoUserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SeasonServiceImpl implements SeasonService {

    private final SeasonRepository seasonRepository;
    private final SeasonMapper seasonMapper;
    private final TechnologyNameToTechnologyEntity technologyNameToTechnology;
    private final UserService userService;
    private final SeasonNotificationService seasonNotificationService;
    private final EmailSendingJob emailSendingJob;

    @Value("${progreso.email.admin}")
    private String adminEmail;

    public SeasonServiceImpl(SeasonRepository seasonRepository,
        SeasonMapper seasonMapper,
        TechnologyNameToTechnologyEntity technologyNameToTechnology,
        UserService userService,
        SeasonNotificationService seasonNotificationService,
        EmailSendingJob emailSendingJob) {
        this.seasonRepository = seasonRepository;
        this.seasonMapper = seasonMapper;
        this.technologyNameToTechnology = technologyNameToTechnology;
        this.userService = userService;
        this.seasonNotificationService = seasonNotificationService;
        this.emailSendingJob = emailSendingJob;
    }

    @Override
    public SeasonResponseDto create(SeasonCreateDto seasonCreateDto) {
        Season season = seasonMapper.fromCreateDto(seasonCreateDto);

        assignOwner(season, seasonCreateDto.ownerId());
        assignUsers(season, seasonCreateDto.mentors(), Role.MENTOR);
        assignUsers(season, seasonCreateDto.interns(), Role.INTERN);
        seasonRepository.save(season);

        scheduleMailSending(season);
        seasonNotificationService.scheduleNotifications(season);

        return seasonMapper.toResponseDto(season);
    }

    @Override
    public SeasonResponseDto getById(Long id) {
        Season season = seasonRepository.findByIdAndFetchUsers(id)
            .orElseThrow(
                () -> new NotFoundException(Season.class.getSimpleName(), "id", id.toString())
            );

        return seasonMapper.toResponseDto(season);
    }

    @Override
    public Page<SeasonResponseDto> getAll(Pageable pageable, ProgresoUserDetails currentUser, SearchSeasonDto searchSeasonDto) {
        if (currentUser.hasRole("ROLE_ADMIN")) {
            return seasonRepository.findAll(new SeasonSpecification(searchSeasonDto), pageable).map(seasonMapper::toResponseDto);
        }
        return seasonRepository.findSeasonsByUserId(pageable, currentUser.getUserId()).map(seasonMapper::toResponseDto);
    }

    @Override
    public SeasonResponseDto update(Long id, SeasonUpdateDto seasonUpdateDto) {
        Season season = seasonRepository.findByIdAndFetchUsers(id)
            .orElseThrow(
                () -> new NotFoundException(Season.class.getSimpleName(), "id", id.toString())
            );

        if (seasonUpdateDto.technologies() != null && !seasonUpdateDto.technologies().isEmpty()) {
            season.setTechnologies(
                technologyNameToTechnology.toTechnologyEntity(seasonUpdateDto.technologies()));
        }

        LocalDate seasonOldStartDate = season.getStartDate();
        LocalDate seasonOldEndDate = season.getEndDate();
        String seasonOldName = season.getName();

        Set<Long> interns = SeasonUtility.getInterns(season).stream().map(User::getId)
            .collect(Collectors.toSet());
        Set<Long> mentors = SeasonUtility.getMentors(season).stream().map(User::getId)
            .collect(Collectors.toSet());

        boolean ownerChanged = isOwnerChanged(season, seasonUpdateDto.ownerId());

        assignUsers(season, seasonUpdateDto.mentors(), Role.MENTOR);
        assignUsers(season, seasonUpdateDto.interns(), Role.INTERN);
        assignOwner(season, seasonUpdateDto.ownerId());

        seasonMapper.updateFromUpdateDto(seasonUpdateDto, season);
        seasonRepository.save(season);

        if (seasonUpdateDto.interns() != null && !interns.equals(seasonUpdateDto.interns())) {
            Set<User> sendNotificationToInterns = seasonNotificationService.sendNotificationsForUsersPerUpdate(
                seasonUpdateDto.interns(), interns);
            sendNotificationToInterns.forEach(
                intern -> seasonNotificationService.notifyUsersAssignedToSeason(season, intern));
        }

        if (seasonUpdateDto.mentors() != null && !mentors.equals(seasonUpdateDto.mentors())) {
            Set<User> sendNotificationToMentors = seasonNotificationService.sendNotificationsForUsersPerUpdate(
                seasonUpdateDto.mentors(), mentors);
            sendNotificationToMentors.forEach(
                mentor -> seasonNotificationService.notifyUsersAssignedToSeason(season, mentor));
        }

        seasonNotificationService.sendNotificationsForStartDatePerUpdate(seasonOldStartDate, season,
            seasonOldName);
        seasonNotificationService.sendNotificationsForEndDatePerUpdate(seasonOldEndDate, season,
            seasonOldName);

        if (ownerChanged) {
            scheduleMailSending(season);
        }

        return seasonMapper.toResponseDto(season);
    }

    @Override
    public void delete(Long id) {
        Season season = seasonRepository.findByIdAndFetchUsers(id).orElseThrow(() ->
            new NotFoundException(Season.class.getSimpleName(), "id", id.toString()));
        seasonRepository.deleteById(season.getId());
        seasonNotificationService.cancelNotificationsForStartDate(season);
        seasonNotificationService.cancelNotificationsForEndDate(season);
    }

    @Override
    public Season findById(Long id) {
        return seasonRepository.findById(id)
            .orElseThrow(
                () -> new NotFoundException(Season.class.getSimpleName(), "id", id.toString()));
    }

    @Override
    public Season findActiveSeason(User requester, Long seasonId) {
        if (requester.getAccount().getRole().equals(Role.INTERN) || requester.getAccount().getRole()
            .equals(Role.MENTOR)) {
            Set<Season> userSeasons = requester.getSeasons();
            if (userSeasons == null) {
                throw new SeasonException("Creator of an event does not belong to any season.");
            }
            return userSeasons.stream()
                .filter(season -> season.getId().equals(seasonId) && season.isActive()).findAny()
                .orElseThrow(() -> new SeasonException("Season with id " + seasonId
                    + " is not active now or user is not assigned to this season."));
        } else {
            Season season = findById(seasonId);
            if (season.isActive()) {
                return season;
            } else {
                throw new SeasonException("Season with id " + seasonId + " is not active now.");
            }
        }
    }

    private void assignUsers(Season season, Set<Long> ids, Role role) {
        if (ids != null) {
            Set<User> toAssign = userService.getAllById(ids);
            SeasonUtility.validateUserStatus(toAssign);
            SeasonUtility.validateUserRoles(toAssign, role);

            if (role.equals(Role.INTERN)) {
                SeasonUtility.validateInterns(season, toAssign);
            }

            Set<User> assignees = SeasonUtility.filterAssigneesByRole(season, role);

            if (season.getUsers() != null && assignees.isEmpty()) {
                season.getUsers().addAll(toAssign);
            } else if (season.getUsers() != null && !assignees.isEmpty()) {
                season.getUsers().removeAll(assignees);
                season.getUsers().addAll(toAssign);
            } else {
                season.setUsers(toAssign);
            }
        }
    }

    private void assignOwner(Season season, Long ownerId) {
        if (ownerId == null) {
            season.setOwner(userService.findByEmail(adminEmail));
        } else {
            User owner = userService.getEntityById(ownerId);
            if ((!owner.isMentor() && !owner.isAdmin()) || !owner.isActive()) {
                throw new OwnerException(
                    "User need to have role ADMIN or MENTOR and to be ACTIVE.");
            } else {
                season.setOwner(owner);
            }
        }
    }

    private boolean isOwnerChanged(Season season, Long ownerId) {
        return !season.getOwner().getId().equals(ownerId);
    }

    private void scheduleMailSending(Season season) {
        String recipient = season.getOwner().getAccount().getEmail();
        String ownerSubject = "Season assigned: " + season.getName();
        Map<String, String> content = Map.of("fullName", season.getOwner().getFullName(), "seasonName",
                season.getName(), "template", "season_owner_email");

        emailSendingJob.scheduleEmailJob(recipient, ownerSubject, content);
    }

    @Override
    public boolean existsById(Long id) {
        if (!seasonRepository.existsById(id)) {
            throw new NotFoundException(Season.class.getSimpleName(), "id", id.toString());
        }
        return true;
    }

    @Override
    public List<Long> getAllSeasonIds() {
        return seasonRepository.getAllSeasonIds();
    }

    @Override
    public List<Long> getSeasonIdsForUser(Long userId) {
        return seasonRepository.getAllSeasonIdsForUser(userId);
    }
}
