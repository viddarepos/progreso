package prime.prime.infrastructure.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import prime.prime.domain.absence.models.AbsenceResponseDto;
import prime.prime.domain.absence.service.AbsenceTrackingService;
import prime.prime.domain.event.models.EventResponseWithAttendeesDto;
import prime.prime.domain.event.service.EventService;
import prime.prime.domain.eventrequest.models.EventRequestReturnDto;
import prime.prime.domain.eventrequest.models.EventRequestStatusDto;
import prime.prime.domain.eventrequest.service.EventRequestService;
import prime.prime.domain.role.Role;
import prime.prime.domain.season.models.SeasonResponseDto;
import prime.prime.domain.season.service.SeasonService;
import prime.prime.domain.technology.entity.Technology;
import prime.prime.domain.user.entity.User;
import prime.prime.domain.user.models.UserCreateDto;
import prime.prime.domain.user.models.UserReturnDto;
import prime.prime.domain.user.models.UserUpdateDto;
import prime.prime.domain.user.service.UserService;
import prime.prime.infrastructure.exception.LastAdminException;
import prime.prime.infrastructure.exception.NoTechnologyException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthorizationService {

    private static final String ADMIN_ROLE = "ROLE_ADMIN";
    private static final String ADMIN = Role.ADMIN.name();
    private final UserService userService;
    private final EventService eventService;
    private final EventRequestService eventRequestService;
    private final SeasonService seasonService;
    private final AbsenceTrackingService absenceTrackingService;

    public AuthorizationService(UserService userService, EventService eventService,
                                EventRequestService eventRequestService, SeasonService seasonService,
                                AbsenceTrackingService absenceTrackingService) {
        this.userService = userService;
        this.eventService = eventService;
        this.eventRequestService = eventRequestService;
        this.seasonService = seasonService;
        this.absenceTrackingService = absenceTrackingService;
    }

    public boolean isAllowedToCreate(UserCreateDto userCreateDto) {
        boolean requestRoleAdmin = userCreateDto.account().role().equals(ADMIN);
        boolean emptyTechnologies = userCreateDto.technologies().isEmpty();

        if (!requestRoleAdmin && emptyTechnologies) {
            throw new NoTechnologyException();
        }
        return !requestRoleAdmin || emptyTechnologies;
    }

    public boolean isAllowedToUpdate(Long id, UserUpdateDto userUpdateDto) {
        User targetUser = getTargetUser(id);
        ProgresoUserDetails currentUser = getCurrentUser();
        String role = getRole(currentUser);

        boolean isCurrentUserAdmin = role.equals(ADMIN_ROLE);
        boolean isTargetUserAdmin = targetUser.getAccount().getRole().equals(Role.ADMIN);
        boolean isOwner = areFromSameSeason(id);

        String targetUserRole = targetUser.getAccount().getRole().name();
        String updateRole =
            userUpdateDto.account() == null || userUpdateDto.account().role() == null
                ? targetUserRole
                : userUpdateDto.account().role();

        Set<String> technologies = userUpdateDto.technologies() == null
            ? targetUser.getTechnologies().stream().map(Technology::getName)
            .collect(Collectors.toSet())
            : userUpdateDto.technologies();

        boolean areTechnologiesChanged = checkChangedTechnologies(technologies,
            targetUser.getTechnologies());

        //Only admins can change role and technologies
        if ((!isCurrentUserAdmin && !isOwner) && (!updateRole.equals(targetUserRole)
            || areTechnologiesChanged)) {
            return false;
        }

        if (isCurrentUserAdmin && !updateRole.equals(ADMIN) && technologies.isEmpty()) {
            throw new NoTechnologyException();
        }

        //Cannot update foreign account
        if ((!isCurrentUserAdmin && !isOwner) && !Objects.equals(targetUser.getId(),
            currentUser.getUserId())) {
            return false;
        }

        //Cannot change admin to intern or mentor without technologies
        if (isTargetUserAdmin && !updateRole.equals(ADMIN) && technologies.isEmpty()) {
            throw new NoTechnologyException();
        }

        //Cannot remove all technologies from intern or mentor
        return isTargetUserAdmin || updateRole.equals(ADMIN) || !technologies.isEmpty();
    }

    public boolean isAllowedToDelete(Long id) {
        User targetUser = getTargetUser(id);

        boolean isTargetUserAdmin = targetUser.getAccount().getEmail().equals("admin@xprmnt.xyz");

        if (isTargetUserAdmin) {
            throw new LastAdminException("Cannot delete the application administrator!");
        }

        return true;
    }

    public boolean isAdminOrOwner(Long id) {
        EventResponseWithAttendeesDto event = eventService.getById(id);
        ProgresoUserDetails currentUser = getCurrentUser();
        String role = getRole(currentUser);

        if (role.equals(ADMIN_ROLE)) {
            return true;
        }

        return event.creatorId().equals(currentUser.getUserId()) || isOwnerOfSeason(
            event.seasonId());
    }

    public boolean isAllowedToDeleteUserImage(Long id) {
        ProgresoUserDetails currentUser = getCurrentUser();
        String role = getRole(currentUser);

        return role.equals(ADMIN_ROLE) || currentUser.getUserId().equals(id);
    }

    public boolean isOwnerOfRequest(Long id) {
        ProgresoUserDetails currentUser = getCurrentUser();
        EventRequestReturnDto eventRequest = eventRequestService.getById(id);

        return Objects.equals(currentUser.getUserId(), eventRequest.requesterId());
    }

    public boolean isOwnerOfAbsenceRequest(Long id) {
        ProgresoUserDetails currentUser = getCurrentUser();
        AbsenceResponseDto absenceRequest = absenceTrackingService.getById(id);

        return Objects.equals(currentUser.getUserId(), absenceRequest.requesterId());
    }


    public boolean isAssignedToRequest(Long id, EventRequestStatusDto eventRequestStatusDto) {
        ProgresoUserDetails currentUser = getCurrentUser();
        EventRequestReturnDto eventRequest = eventRequestService.getById(id);

        return (Objects.equals(currentUser.getUserId(), eventRequest.assigneeId()) ||
            isAllowedToChangeEventRequest(id))
            &&
            !(eventRequest.status().equals("APPROVED") && eventRequestStatusDto.status()
                .isApproved());
    }

    private boolean checkChangedTechnologies(Set<String> technologies,
        Set<Technology> targetUserTechnologies) {
        if (technologies.size() != targetUserTechnologies.size()) {
            return true;
        }

        Set<String> technologyNames = targetUserTechnologies.stream().map(Technology::getName)
            .collect(Collectors.toSet());

        return !technologyNames.containsAll(technologies);
    }

    public boolean isAssignedToSeason(Long id) {
        SeasonResponseDto seasonResponseDto = seasonService.getById(id);
        return isAssignedToSeason(seasonResponseDto) || isOwner(seasonResponseDto);
    }

    public boolean isAssignedToSeason(SeasonResponseDto seasonResponseDto) {
        ProgresoUserDetails currentUser = getCurrentUser();
        Set<Long> assignedIds = new HashSet<>();

        if (seasonResponseDto.mentors() != null) {
            var mentorIds = seasonResponseDto.mentors()
                .stream()
                .map(UserReturnDto::id)
                .toList();
            assignedIds.addAll(mentorIds);
        }

        if (seasonResponseDto.interns() != null) {
            var internIds = seasonResponseDto.interns()
                .stream()
                .map(UserReturnDto::id)
                .toList();
            assignedIds.addAll(internIds);
        }

        return assignedIds.contains(currentUser.getUserId());
    }

    public boolean isOwnerOfSeason(Long id) {
        SeasonResponseDto seasonResponseDto = seasonService.getById(id);
        return isOwner(seasonResponseDto);
    }

    public boolean isAllowedToChangeEventRequest(Long id) {
        EventRequestReturnDto eventRequestReturnDto = eventRequestService.getById(id);
        return isOwnerOfSeason(eventRequestReturnDto.seasonId());
    }

    public boolean areFromSameSeason(Long id) {
        User user = userService.getEntityById(id);
        ProgresoUserDetails currentUser = getCurrentUser();
        if (user.getSeasons() == null) {
            return false;
        }
        return user.getSeasons().stream()
            .anyMatch(season -> season.getOwner().getId().equals(currentUser.getUserId()));
    }

    public boolean canAccessAbsenceRequest(Long id) {

        ProgresoUserDetails currentUser = getCurrentUser();

        User user = userService.getEntityById(currentUser.getUserId());
        if (user.getSeasons() == null) {
            return false;
        }

        AbsenceResponseDto absenceRequest = absenceTrackingService.getById(id);
        return user.getSeasons().stream()
                .anyMatch(season -> season.getId().equals(absenceRequest.seasonId()));
    }

    public boolean canAccessEventRequest(Long id) {

        ProgresoUserDetails currentUser = getCurrentUser();
        User user = userService.getEntityById(currentUser.getUserId());
        if (user.getSeasons() == null) {
            return false;
        }

        var eventRequest = eventRequestService.getById(id);
        if(isOwnerOfSeason(eventRequest.seasonId())) {
            return true;
        }

        return user.getSeasons().stream()
                .anyMatch(season -> season.getId().equals(eventRequest.seasonId()));
    }

    private boolean isOwner(SeasonResponseDto seasonResponseDto) {
        ProgresoUserDetails currentUser = getCurrentUser();
        return currentUser.getUserId().equals(seasonResponseDto.owner().id());
    }

    private User getTargetUser(Long id) {
        return userService.getEntityById(id);
    }

    public ProgresoUserDetails getCurrentUser() {
        return (ProgresoUserDetails) SecurityContextHolder.getContext().getAuthentication()
            .getPrincipal();
    }

    private static String getRole(ProgresoUserDetails currentUser) {
        return currentUser.getAuthorities()
            .stream()
            .findFirst()
            .get()
            .getAuthority();
    }

    public boolean isAllowedToChangeAbsenceRequest(Long id){
        AbsenceResponseDto absenceResponseDto = absenceTrackingService.getById(id);
        return isOwnerOfSeason(absenceResponseDto.seasonId());
    }

}
