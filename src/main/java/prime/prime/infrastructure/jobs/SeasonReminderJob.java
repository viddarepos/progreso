package prime.prime.infrastructure.jobs;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import prime.prime.domain.role.Role;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.user.entity.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class SeasonReminderJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(SeasonReminderJob.class);
    private static final String TEMPLATE = "season_notification_email";
    private final Job job;
    private final EmailSendingJob emailSendingJob;

    public SeasonReminderJob(Job job, EmailSendingJob emailSendingJob) {
        this.job = job;
        this.emailSendingJob = emailSendingJob;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        logger.info("Executing Job with key {}", context.getJobDetail().getKey());
        JobDataMap jobDataMap = context.getMergedJobDataMap();

        Map<String, String> content = Map.of("fullName", jobDataMap.getString("fullName"), "name",
                jobDataMap.getString("name"), "information", jobDataMap.getString("information"), "startDate",
                jobDataMap.getString("startDate"), "endDate", jobDataMap.getString("endDate"), "template",
                TEMPLATE);
        emailSendingJob.scheduleEmailJob(jobDataMap.getString("email"), jobDataMap.getString("subject"), content);
    }

    public JobDetail buildJobDetail(User user, Season season, String information, String date,
        String identityName) {
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("email", user.getAccount().getEmail());
        jobDataMap.put("subject", "Season Reminder");
        jobDataMap.put("fullName", user.getFullName());
        jobDataMap.put("name", season.getName());
        jobDataMap.put("information", information);
        jobDataMap.put("startDate",
            season.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        jobDataMap.put("endDate",
            season.getEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        jobDataMap.put("date", date);
        String identify = "seasonReminder-jobs";
        String description = "Season reminder job";

        return job.buildJobDetail(jobDataMap, identify, description, SeasonReminderJob.class,
            identityName);
    }

    public Trigger buildJobTrigger(JobDetail jobDetail, LocalDate creationDate) {

        String identify = "seasonReminder-triggers";
        String description = "send season trigger";

        return job.buildJobTrigger(jobDetail, creationDate, identify, description);
    }

    public void scheduleJob(JobDetail jobDetail, Trigger trigger, User user, Season season,
        String additionalInfo) {
        String message = "Season reminder scheduled successfully";
        String userEmail = user.getAccount().getEmail();
        Role userRole = user.getAccount().getRole();
        job.scheduleJob(jobDetail, trigger, message);
        logger.info("User email:" + userEmail
            + " with role:" + userRole
            + " for season id:" + season.getId()
            + " " + additionalInfo);

    }

    public void  cancelJob(JobKey jobKey) {
        String message = "Season reminder successfully canceled";
        job.cancelJob(jobKey, message);
    }
}
