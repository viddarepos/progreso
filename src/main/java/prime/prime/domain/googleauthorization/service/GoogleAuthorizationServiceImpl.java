package prime.prime.domain.googleauthorization.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import prime.prime.domain.googleauthorization.entity.GoogleAuthorization;
import prime.prime.domain.googleauthorization.repository.GoogleAuthorizationRepository;
import prime.prime.infrastructure.exception.GoogleAuthorizationException;

@Service
public class GoogleAuthorizationServiceImpl implements GoogleAuthorizationService {

    private final String clientId;
    private final String clientSecret;
    private final String grantType;
    private final GoogleAuthorizationRepository googleAuthorizationRepository;
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();


    @Autowired
    public GoogleAuthorizationServiceImpl(
        @Value("${google.service.client.id}") String clientId,
        @Value("${google.service.client.secret}") String clientSecret,
        @Value("${google.service.grant.type}") String grantType,
        GoogleAuthorizationRepository googleAuthorizationRepository) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.grantType = grantType;
        this.googleAuthorizationRepository = googleAuthorizationRepository;
    }

    private void getCredentials() throws IOException {
        Details web = new Details();
        web.setClientId(clientId);
        web.setClientSecret(clientSecret);
        GoogleClientSecrets clientSecrets = new GoogleClientSecrets().setWeb(web);
        HttpTransport httpTransport;

        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleAuthorizationException("Invalid credentials");
        }

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport,
            JSON_FACTORY, clientSecrets,
            Collections.singleton(CalendarScopes.CALENDAR)).setAccessType("offline").build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential googleCredentials = new AuthorizationCodeInstalledApp(flow, receiver).authorize(
            "user");
        saveGoogleAccessTokenAndRefreshTokenToDatabase(new GoogleAuthorization(),
            googleCredentials.getAccessToken(), googleCredentials.getRefreshToken(),
            googleCredentials.getExpiresInSeconds());
    }

    private void saveGoogleAccessTokenAndRefreshTokenToDatabase(
        GoogleAuthorization googleAuthorization, String accessToken, String refreshToken,
        Long expirationTime) {
        googleAuthorization.setAccessToken(accessToken);
        googleAuthorization.setRefreshToken(refreshToken);
        googleAuthorization.setExpirationDateTime(LocalDateTime.now().plusSeconds(expirationTime));
        googleAuthorizationRepository.save(googleAuthorization);
    }

    private void updateGoogleAuthorization(TokenResponse response,
        GoogleAuthorization googleAuthorization) {
        String accessToken = response.getAccessToken();
        Long expiresIn = response.getExpiresInSeconds();
        LocalDateTime expirationDate = LocalDateTime.now().plusSeconds(expiresIn);
        googleAuthorization.setExpirationDateTime(expirationDate);
        googleAuthorization.setAccessToken(accessToken);
        googleAuthorizationRepository.save(googleAuthorization);
    }

    public void refreshAccessTokenAndSave(GoogleAuthorization googleAuthorization)
        throws IOException {
        TokenResponse response = new GoogleRefreshTokenRequest(new NetHttpTransport(),
            new GsonFactory(),
            googleAuthorization.getRefreshToken(), clientId, clientSecret).setGrantType(grantType)
            .execute();
        updateGoogleAuthorization(response, googleAuthorization);
    }

    public HttpRequestInitializer createAccessToken(String accessToken,
        LocalDateTime expirationDate) {
        Instant instant = Instant.from(expirationDate.atZone(ZoneId.systemDefault()));
        GoogleCredentials credentials = GoogleCredentials.create(
            new AccessToken(accessToken, Date.from(instant)));
        return new HttpCredentialsAdapter(credentials);
    }

    @Override
    public GoogleAuthorization getFirstRecordFromGoogleAuthorizationTable() throws IOException {
        Collection<GoogleAuthorization> googleAuthorizationList = googleAuthorizationRepository.findAll()
            .stream().toList();
        if (googleAuthorizationList.isEmpty()) {
            getCredentials();
        }
        return googleAuthorizationRepository.findAll().get(0);
    }
}
