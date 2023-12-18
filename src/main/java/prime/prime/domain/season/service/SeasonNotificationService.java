package prime.prime.domain.season.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.season.utility.SeasonUtility;
import prime.prime.domain.user.entity.User;
import prime.prime.domain.user.service.UserService;
import prime.prime.infrastructure.jobs.SeasonReminderJob;

@Service
public class SeasonNotificationService {

    private final UserService userService;
    private final SeasonReminderJob seasonReminderJob;

    @Autowired
    public SeasonNotificationService(UserService userService, SeasonReminderJob seasonReminderJob) {
        this.userService = userService;
        this.seasonReminderJob = seasonReminderJob;
    }

    void scheduleNotifications(Season season) {
        if (season.getUsers() == null || season.getUsers().isEmpty()) {
            return;
        }
        Set<User> users = season.getUsers();
        for (User user : users) {
            notifyUsersAssignedToSeason(season, user);
            notifyUsersForSeasonEnd(season, user);
            if (user.isMentor()) {
                notifyUsersForSeasonStart(season, user);
            }
        }
        for (User admin : userService.getAllAdmins()) {
            notifyUsersForSeasonStart(season, admin);
        }
    }

    void notifyUsersAssignedToSeason(Season season, User user) {
        JobDetail jobDetail = seasonReminderJob.buildJobDetail(user, season,
            "You received that email to inform you that you are assigned to a season."
                + "You will be provided with additional information about it.",
            LocalDate.now().toString(), UUID.randomUUID().toString());
        Trigger trigger = seasonReminderJob.buildJobTrigger(jobDetail, LocalDate.now());
        seasonReminderJob.scheduleJob(jobDetail, trigger, user, season,
            "User is assigned to season");
    }

    void notifyUsersForSeasonStart(Season season, User user) {
        if (!isDateValidForSendNotifications(season.getStartDate())) {
            return;
        }
        LocalDate twoWeeksBeforeSeasonStart = season.getStartDate()
            .minus(2, ChronoUnit.WEEKS);
        JobDetail jobDetail = seasonReminderJob.buildJobDetail(user, season,
            "You received this email to inform you that there is upcoming season that you are part of "
                + "and there are two weeks due to its start. ",
            season.getStartDate().toString(),
            user.getAccount().getEmail() + "startDate" + season.getId());
        Trigger trigger = seasonReminderJob.buildJobTrigger(jobDetail,
            twoWeeksBeforeSeasonStart);
        seasonReminderJob.scheduleJob(jobDetail, trigger, user, season,
            "Notification two weeks before season start");
    }


    void notifyUsersForSeasonEnd(Season season, User user) {
        if (!isDateValidForSendNotifications(season.getEndDate())) {
            return;
        }
        LocalDate twoWeeksBeforeSeasonEnd = season.getEndDate().minus(2, ChronoUnit.WEEKS);
        JobDetail jobDetail = seasonReminderJob.buildJobDetail(user, season,
            "You received this notification to inform you that there is upcoming events related to the season you are participating in. There two weeks left to its end date.",
            season.getEndDate().toString(),
            user.getAccount().getEmail() + "endDate" + season.getId());
        Trigger trigger = seasonReminderJob.buildJobTrigger(jobDetail,
            twoWeeksBeforeSeasonEnd);
        seasonReminderJob.scheduleJob(jobDetail, trigger, user, season,
            "Notification two weeks before season end");
    }

    Set<User> sendNotificationsForUsersPerUpdate(Set<Long> dtoUsers,
        Set<Long> seasonUsers) {
        Set<Long> usersToAdd = dtoUsers.stream()
            .filter(user -> !seasonUsers.contains(user))
            .collect(Collectors.toSet());
        if (!seasonUsers.equals(usersToAdd)) {
            return userService.getAllById(usersToAdd);
        }
        return new HashSet<>();
    }

    public void sendNotificationsForStartDatePerUpdate(LocalDate startDate, Season season,
        String seasonName) {
        if (!startDate.equals(season.getStartDate()) || !seasonName.equals(season.getName())) {
            cancelNotificationsForStartDate(season);
            if (isDateValidForSendNotifications(season.getStartDate())) {
                filterMentorsAndAdmins(season).forEach(
                    user -> notifyUsersForSeasonStart(season, user));
            }
        }
    }

    public void sendNotificationsForEndDatePerUpdate(LocalDate endDate, Season season,
        String seasonName) {
        if (!endDate.equals(season.getEndDate()) || !seasonName.equals(season.getName())) {
            cancelNotificationsForEndDate(season);
            if (isDateValidForSendNotifications(season.getEndDate())) {
                season.getUsers().forEach(user -> notifyUsersForSeasonEnd(season, user));
            }
        }
    }

    void cancelNotificationsForEndDate(Season season) {
        season.getUsers().forEach(user -> seasonReminderJob.cancelJob(
            JobKey.jobKey(user.getAccount().getEmail() + "endDate" + season.getId(),
                "email-jobs")));
    }

    void cancelNotificationsForStartDate(Season season) {
        filterMentorsAndAdmins(season).forEach(user -> seasonReminderJob.cancelJob(
            JobKey.jobKey(user.getAccount().getEmail() + "startDate" + season.getId(),
                "email-jobs")));
    }

    Set<User> filterMentorsAndAdmins(Season season) {
        Set<User> mentorsAndAdmins = SeasonUtility.getMentors(season);
        mentorsAndAdmins.addAll(userService.getAllAdmins());
        return mentorsAndAdmins;
    }

    private boolean isDateValidForSendNotifications(LocalDate date) {
        return date.isAfter(LocalDate.now().plusDays(13));
    }
}
