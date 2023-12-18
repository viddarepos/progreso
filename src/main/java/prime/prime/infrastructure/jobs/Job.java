package prime.prime.infrastructure.jobs;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

@Component
public class Job {

    private static final Logger logger = LoggerFactory.getLogger(Job.class);
    private final Scheduler scheduler;

    public Job(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void scheduleJob(JobDetail jobDetail, Trigger trigger, String message) {
        try {
            scheduler.scheduleJob(jobDetail, trigger);
            logger.info(message);
        } catch (SchedulerException e) {
            logger.error("Error scheduling job", e);
        }
    }

    public void cancelJob(JobKey jobKey, String message) {
        try {
            scheduler.deleteJob(jobKey);
            logger.info(message);
        } catch (SchedulerException e) {
            logger.error("Error deleting job", e);
        }
    }

    public JobDetail buildJobDetail(JobDataMap jobDataMap, String identify, String description,
        Class<? extends org.quartz.Job> jobClass, String identityName) {

        return JobBuilder.newJob(jobClass)
            .withIdentity(identityName, identify)
            .withDescription(description)
            .usingJobData(jobDataMap)
            .storeDurably()
            .build();
    }

    public Trigger buildJobTrigger(JobDetail jobDetail, LocalDate creationDate, String identify,
        String description) {

        return TriggerBuilder.newTrigger()
            .forJob(jobDetail)
            .withIdentity(jobDetail.getKey().getName(), identify)
            .withDescription(description)
            .startAt(Date.from(creationDate.atStartOfDay().toInstant(ZoneOffset.UTC)))
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withMisfireHandlingInstructionFireNow())
            .build();
    }

    public Trigger buildJobTrigger(JobDetail jobDetail, Date startTime, String identify,
                                   String description) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), identify)
                .withDescription(description)
                .startAt(startTime)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();
    }
}
