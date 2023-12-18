package prime.prime.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final ProgresoUserDetailsService progresoUserDetailsService;
    private final JwtUtil jwtUtil;

    public AuthenticationFilter(ProgresoUserDetailsService progresoUserDetailsService, JwtUtil jwtUtil) {
        this.progresoUserDetailsService = progresoUserDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String email = jwtUtil.getUserEmailFromHeader(request);

            UserDetails userDetails = progresoUserDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
        } catch (Exception ex) {
            LOGGER.error("Cannot set authentication", ex);
        }

        filterChain.doFilter(request, response);
    }
}
