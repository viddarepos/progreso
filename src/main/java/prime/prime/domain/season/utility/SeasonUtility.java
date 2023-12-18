package prime.prime.domain.season.utility;

import prime.prime.domain.account.entity.AccountStatus;
import prime.prime.domain.role.Role;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.user.entity.User;
import prime.prime.infrastructure.exception.InvalidDateException;
import prime.prime.infrastructure.exception.SeasonException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;

public class SeasonUtility {

    private static final int MAX_SEASON_COUNT_PER_INTERN = 3;

    public static void validateStartDateIsBeforeEndDate(LocalDate startDate, LocalDate endDate) {
        if (!startDate.isBefore(endDate)) {
            throw new InvalidDateException("Start date cannot be after or the same as end date");
        }
    }

    public static void validateUserRoles(Set<User> users, Role role) {
        var invalidUsers = users
            .stream()
            .filter(user -> !user.getAccount().getRole().equals(role))
            .map(User::getId)
            .toList();

        if (!invalidUsers.isEmpty()) {
            throw new SeasonException(User.class.getSimpleName(), "id", invalidUsers.toString(),
                role);
        }
    }

    public static void validateUserStatus(Set<User> users) {
        var invalidUsers = users
            .stream()
            .filter(user -> !user.getAccount().getStatus().equals(AccountStatus.ACTIVE))
            .map(User::getId)
            .toList();

        if (!invalidUsers.isEmpty()) {
            throw new SeasonException(User.class.getSimpleName(), "id", invalidUsers.toString());
        }
    }

    public static void validateInterns(Season season, Set<User> interns) {
        var invalidInterns = interns
            .stream()
            .filter(intern -> (intern.getSeasons().size() >= MAX_SEASON_COUNT_PER_INTERN))
            .map(User::getId)
            .toList();

        if (season.getUsers() != null) {
            var userIds = season.getUsers()
                .stream()
                .map(User::getId)
                .collect(Collectors.toSet());

            invalidInterns = invalidInterns
                .stream()
                .filter(id -> !userIds.contains(id))
                .toList();
        }

        if (!invalidInterns.isEmpty()) {
            throw new SeasonException("Interns", "id", invalidInterns.toString(),
                MAX_SEASON_COUNT_PER_INTERN);
        }
    }

    public static Set<User> filterAssigneesByRole(Season season, Role role) {
        if (season.getUsers() == null) {
            return null;
        }

        return season.getUsers().stream()
            .filter(user -> user.getAccount().getRole().equals(role))
            .collect(Collectors.toSet());
    }

    public static Set<User> getInterns(Season season) {
        return season.getUsers().stream()
            .filter(user -> user.getAccount().getRole().equals(Role.INTERN))
            .collect(Collectors.toSet());
    }

    public static Set<User> getMentors(Season season) {
        return season.getUsers().stream()
            .filter(user -> user.getAccount().getRole().equals(Role.MENTOR))
            .collect(Collectors.toSet());
    }

    public static void validateSeasons(User user, Long seasonId) {
        if (user.isMentor() && nonAssignedSeasons(user.getSeasons(), seasonId)
            && notOwnerOfSeasons(user.getSeasonsOwner(), seasonId)) {
            throw new SeasonException(user.getId(), seasonId);
        }
        if (user.isIntern() && nonAssignedSeasons(user.getSeasons(), seasonId)) {
            throw new SeasonException(user.getId(), seasonId);
        }
    }

    private static boolean nonAssignedSeasons(Set<Season> seasons, Long seasonId) {
        return seasons == null || seasons.stream()
            .noneMatch(season -> season.getId().equals(seasonId));
    }

    private static boolean notOwnerOfSeasons(Set<Season> seasons, Long seasonId) {
        return seasons == null || seasons.stream()
            .noneMatch(seasonOwner -> seasonOwner.getId().equals(seasonId));
    }

    public static boolean isWithinSeason(Season season, LocalDateTime startTime, LocalDateTime endTime) {
        return (startTime.isEqual(season.getStartDate().atStartOfDay()) || startTime.isAfter(season.getStartDate().atStartOfDay())) &&
                (endTime.isEqual(season.getEndDate().atTime(LocalTime.MAX)) || endTime.isBefore(season.getEndDate().atTime(LocalTime.MAX)));
    }

    public static boolean isWithinSeason(Season season, LocalDateTime localDateTime) {
        return localDateTime.isEqual(season.getStartDate().atStartOfDay()) || (localDateTime.isAfter(season.getStartDate().atStartOfDay()) &&
                localDateTime.isBefore(season.getEndDate().atTime(LocalTime.MAX))) || localDateTime.isEqual(season.getEndDate().atTime(LocalTime.MAX));
    }
    public static boolean isUserWithinSeason(Long userId, Season season) {
        return season.getUsers().stream().anyMatch(user -> userId.equals(user.getId()));
    }
}
