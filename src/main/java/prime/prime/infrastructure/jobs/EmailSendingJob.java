package prime.prime.infrastructure.jobs;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import prime.prime.infrastructure.email_sender.config.Email;
import prime.prime.infrastructure.email_sender.service.EmailService;
import prime.prime.infrastructure.exception.EmailSendException;

import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class EmailSendingJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(EmailSendingJob.class);
    private final Job job;
    private final EmailService emailService;

    public EmailSendingJob(Job job, EmailService emailService) {
        this.job = job;
        this.emailService = emailService;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        String toEmail = jobDataMap.getString("email");
        String subject = jobDataMap.getString("subject");
        String identifier = jobDataMap.getString("uniqueJobName");
        Map<String,String> content = (Map<String, String>) jobDataMap.get("content");

        Email emailToSend = new Email(toEmail, subject, content);

        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                logger.info("Sending Email to {}", toEmail);
                emailService.send(emailToSend);
                logger.info("Email was successfully sent to {}", toEmail);
                break;
            } catch (EmailSendException e) {
                logger.info("Error in sending mail to {}. Retrying...", toEmail);
                retryCount++;
            }
        }

        if (retryCount == maxRetries) {
            logger.info("Failed to send email to {} after {} retries. Notifying admin...", toEmail, maxRetries);
            Email adminNotification = new Email("admin@int-team.protal.biz", "Email sending failed", Map.of("email",
                    toEmail, "template", "admin_notification_email_sending_fail.html"));
            emailService.send(adminNotification);
            job.cancelJob(JobKey.jobKey(identifier,"emailSending-jobs"),"Email sending failed for three times. Deleting job...");
        }
    }

    private JobDetail buildJobDetail(String email,String subject, String creationDate,Map<String,String> content) {
        JobDataMap jobDataMap = new JobDataMap();
        String identifier = UUID.randomUUID().toString();
        jobDataMap.put("email", email);
        jobDataMap.put("subject", subject);
        jobDataMap.put("creationDate", creationDate);
        jobDataMap.put("content",content);
        jobDataMap.put("uniqueJobName", identifier);

        String identify = "emailSending-jobs";
        String description = "Send email job";

        return job.buildJobDetail(jobDataMap, identify, description, EmailSendingJob.class,
               identifier);
    }

    private Trigger buildJobTrigger(JobDetail jobDetail, Date creationDate) {
        String identify = "sendEmail-triggers";
        String description = "send email trigger";

        return job.buildJobTrigger(jobDetail, creationDate, identify, description);
    }

    private void scheduleJob(JobDetail jobDetail, Trigger trigger) {
        String message = "Email sending scheduled successfully";
        job.scheduleJob(jobDetail, trigger, message);
    }

    public void scheduleEmailJob(String email,String subject,Map<String,String> content) {
            JobDetail jobDetail = buildJobDetail(email, subject,
                    LocalDate.now().toString(), content);
            Trigger trigger = buildJobTrigger(jobDetail, new Date());
            scheduleJob(jobDetail, trigger);
    }
}