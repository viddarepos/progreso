package prime.prime.infrastructure.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class ApiKeyAuthManager implements AuthenticationManager {

    private final String apiKey;

    public ApiKeyAuthManager(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String principal = (String) authentication.getPrincipal();

        if (!apiKey.equals(principal)) {
            throw new BadCredentialsException("Invalid API key");
        }

        authentication.setAuthenticated(true);
        return authentication;
    }
}
