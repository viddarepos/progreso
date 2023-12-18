package prime.prime.infrastructure.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import prime.prime.domain.user.repository.Projection.UserProjection;
import prime.prime.domain.user.service.UserService;

import java.util.Collections;

@Service
public class ProgresoUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public ProgresoUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserProjection response = userService.findUserByAccountEmail(email);

        return new ProgresoUserDetails(response.getAccountId(), response.getId(), response.getAccountEmail(), response.getAccountPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + response.getAccountRole())));
    }
}
