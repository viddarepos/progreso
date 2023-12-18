package prime.prime.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class JwtUtil {

    @Value("${java-api.http.user-email-header}")
    private String userEmail;

    public String getUserEmailFromHeader(HttpServletRequest request) {
       return request.getHeader(userEmail);
    }
}
