package prime.prime.domain.googleAuthorization.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import prime.prime.domain.googleauthorization.entity.GoogleAuthorization;
import prime.prime.domain.googleauthorization.repository.GoogleAuthorizationRepository;
import prime.prime.domain.googleauthorization.service.GoogleAuthorizationServiceImpl;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GoogleAuthorizationServiceImplTest {

    @Mock
    private GoogleAuthorizationRepository googleAuthorizationRepository;
    private final List<GoogleAuthorization> googleAuthorizationList = new ArrayList<>();
    @InjectMocks
    private GoogleAuthorizationServiceImpl googleAuthorizationService;
    private final GoogleAuthorization googleAuthorization=new GoogleAuthorization();

    @Test
    void getFirstRecord_ByIndex_Successful() throws IOException {
        googleAuthorizationList.add(googleAuthorization);
        when(googleAuthorizationRepository.findAll()).thenReturn(googleAuthorizationList);
        GoogleAuthorization result = googleAuthorizationService.getFirstRecordFromGoogleAuthorizationTable();

        assertEquals(googleAuthorizationList.get(0), result);
    }
}
