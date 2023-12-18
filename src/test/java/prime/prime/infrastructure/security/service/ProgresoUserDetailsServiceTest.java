package prime.prime.infrastructure.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import prime.prime.domain.account.entity.AccountStatus;
import prime.prime.domain.user.repository.Projection.UserProjection;
import prime.prime.domain.user.service.UserServiceImpl;
import prime.prime.infrastructure.exception.NotFoundException;
import prime.prime.infrastructure.security.ProgresoUserDetails;
import prime.prime.infrastructure.security.ProgresoUserDetailsService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProgresoUserDetailsServiceTest {

    @InjectMocks
    ProgresoUserDetailsService progresoUserDetailsService;
    @Mock
    private UserServiceImpl userService;
    private static final ProjectionFactory factory = new SpelAwareProxyProjectionFactory();

    @Test
    void loadUserByUsername_ExistingEmail_Successful() {
        UserProjection userProjection = factory.createProjection(UserProjection.class);
        userProjection.setAccountId(1L);
        userProjection.setId(1L);
        userProjection.setAccountEmail("admin@progreso.com");
        userProjection.setAccountPassword("Admin123+");
        userProjection.setAccountStatus(AccountStatus.ACTIVE);
        userProjection.setAccountRole("ROLE_ADMIN");

        when(userService.findUserByAccountEmail("admin@progreso.com")).thenReturn(userProjection);

        ProgresoUserDetails userDetails = (ProgresoUserDetails) progresoUserDetailsService.loadUserByUsername(
            "admin@progreso.com");

        assertEquals("admin@progreso.com", userDetails.getUsername());
        assertEquals(1L, userDetails.getUserId());
    }

    @Test
    void loadUserByUsername_NonExistingEmail_ThrowsNotFoundException() {
        when(userService.findUserByAccountEmail("mentor@progreso.com")).thenThrow(
            NotFoundException.class);

        assertThrows(NotFoundException.class,
            () -> progresoUserDetailsService.loadUserByUsername("mentor@progreso.com"));
    }
}
