package prime.prime.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.net.URI;
import prime.prime.infrastructure.utility.PropertiesExtractor;

public class InvalidOldPasswordException extends ErrorResponseException {

    public InvalidOldPasswordException() {
        super(HttpStatus.CONFLICT, asProblemDetail("Incorrect old password",
            PropertiesExtractor.getProperty("java-api.problem-definitions-url")), null);
    }

    private static ProblemDetail asProblemDetail(String message, String problemUrl) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Invalid old password");
        problemDetail.setType(URI.create(problemUrl + "invalid-old-password"));
        return problemDetail;
    }
}
