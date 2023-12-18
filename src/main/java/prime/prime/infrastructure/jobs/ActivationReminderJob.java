package prime.prime.infrastructure.jobs;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import prime.prime.domain.account.entity.Account;
import prime.prime.domain.account.entity.AccountStatus;
import prime.prime.domain.account.repository.AccountRepository;
import prime.prime.domain.user.entity.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Component
public class ActivationReminderJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(ActivationReminderJob.class);
    private static final String TEMPLATE = "reminder_email_for_login";
    private final Job job;
    private final AccountRepository accountRepository;
    private final EmailSendingJob emailSendingJob;

    public ActivationReminderJob(Job job, AccountRepository accountRepository,
                                 EmailSendingJob emailSendingJob) {
        this.job = job;
        this.accountRepository = accountRepository;
        this.emailSendingJob = emailSendingJob;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        logger.info("Executing Job with key {}", context.getJobDetail().getKey());

        JobDataMap jobDataMap = context.getMergedJobDataMap();
        String toEmail = jobDataMap.getString("email");
        Account account = accountRepository.findByEmail(toEmail);

        if (account.getStatus().equals(AccountStatus.INVITED)) {
            String name = jobDataMap.getString("fullname");
            String subject = jobDataMap.getString("subject");
            String date = jobDataMap.getString("creationDate");
            scheduleMailSending(toEmail, name, subject, date);
        } else {
            logger.info("The user has active account");
        }
    }

    private void scheduleMailSending(String toEmail, String name, String subject, String date) {
        Map<String, String> content = Map.of("fullName", name, "creationDate", date, "template", TEMPLATE);
        emailSendingJob.scheduleEmailJob(toEmail, subject, content);
    }

    public JobDetail buildJobDetail(User user, LocalDate creationDate) {
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("email", user.getAccount().getEmail());
        jobDataMap.put("subject", "Activation reminder");
        jobDataMap.put("fullname", user.getFullName());
        jobDataMap.put("creationDate", creationDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        String identify = "accountActivation-jobs";
        String description = "Account activation job";

        return job.buildJobDetail(jobDataMap, identify, description, ActivationReminderJob.class,
            UUID.randomUUID().toString());
    }

    public Trigger buildJobTrigger(JobDetail jobDetail, LocalDate creationDate) {

        String identify = "activationReminder-triggers";
        String description = "send activation trigger";

        return job.buildJobTrigger(jobDetail, creationDate, identify, description);
    }

    public void scheduleJob(JobDetail jobDetail, Trigger trigger) {
        String message = "Activation reminder scheduled successfully";
        job.scheduleJob(jobDetail, trigger, message);
    }
}
