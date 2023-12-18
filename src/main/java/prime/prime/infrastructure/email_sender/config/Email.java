package prime.prime.infrastructure.email_sender.config;

import java.util.Map;

public record Email(
        String to,
        String subject,
        Map<String, String> content
) {
}
