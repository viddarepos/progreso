package prime.prime.domain.googleauthorization.service;

import java.io.IOException;
import prime.prime.domain.googleauthorization.entity.GoogleAuthorization;

public interface GoogleAuthorizationService {
   GoogleAuthorization getFirstRecordFromGoogleAuthorizationTable() throws IOException;


}
