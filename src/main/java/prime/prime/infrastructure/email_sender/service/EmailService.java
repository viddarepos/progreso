package prime.prime.infrastructure.email_sender.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import prime.prime.infrastructure.email_sender.config.Email;
import prime.prime.infrastructure.exception.EmailSendException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Service
public class EmailService {
    @Value("${spring.mail.username}")
    private String from;
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    public void send(Email email) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

        try {
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(email.to());
            mimeMessageHelper.setSubject(email.subject());
            mimeMessageHelper.setText(generateMessageContent(email.content()), true);
            javaMailSender.send(mimeMessageHelper.getMimeMessage());
        } catch (MessagingException e) {
            throw new EmailSendException(e.getMessage());
        }

    }

    private String generateMessageContent(Map<String, String> content) {
        Context context = new Context();
        boolean containsMessage = content.containsKey("message");
        boolean notContainsMassageOrTemplate = !containsMessage && !content.containsKey("template");

        if (notContainsMassageOrTemplate) {
            throw new EmailSendException("Email content must contain either message or template!");
        }

        if (containsMessage) {
            return content.get("message");
        }

        content.forEach(context::setVariable);

        return templateEngine.process(content.get("template"), context);
    }
}
