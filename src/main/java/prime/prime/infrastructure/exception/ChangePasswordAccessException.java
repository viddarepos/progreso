package prime.prime.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.net.URI;
import prime.prime.infrastructure.utility.PropertiesExtractor;

public class ChangePasswordAccessException extends ErrorResponseException {

    public ChangePasswordAccessException() {
        super(HttpStatus.CONFLICT, asProblemDetail("User can change its own password only",
            PropertiesExtractor.getProperty("java-api.problem-definitions-url")), null);
    }

    private static ProblemDetail asProblemDetail(String message, String problemUrl) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Can't change password");
        problemDetail.setType(URI.create(problemUrl + "change-pasword-access"));
        return problemDetail;
    }
}
