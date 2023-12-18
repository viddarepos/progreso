package prime.prime.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity()
public class SecurityConfig {
    private static final String[] SWAGGER_WHITELIST = {
            "/swagger-config.yaml", "/v3/api-docs/**",
            "/swagger-ui.html", "/swagger-ui/**", "/swagger-resources/**",
            "/swagger-resources/configuration/ui"};
    @Value("${java-api.http.api-key-header}")
    private String apiKeyRequestHeader;
    @Value("${java-api.http.api-key-value}")
    private String apiKeyRequestValue;

    public SecurityConfig( ) {}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.cors(withDefaults()).csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(SWAGGER_WHITELIST).permitAll();
                    auth.requestMatchers("/profile_pictures/**").permitAll();
                    auth.requestMatchers("/test").permitAll();
                    auth.requestMatchers("/testJavaGatewayCommunication").permitAll();
                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(excHandling -> excHandling.authenticationEntryPoint(new BasicAuthenticationEntryPoint()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers((headers) ->
                        headers
                                .contentTypeOptions(withDefaults())
                                .xssProtection(withDefaults())
                                .cacheControl(withDefaults())
                                .httpStrictTransportSecurity(withDefaults())
                                .frameOptions(withDefaults()
                                ));

        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(apiKeyRequestHeader);
        filter.setAuthenticationManager(new ApiKeyAuthManager(apiKeyRequestValue));
        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }
}
