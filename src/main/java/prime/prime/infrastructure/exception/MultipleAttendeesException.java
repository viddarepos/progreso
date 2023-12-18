package prime.prime.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.net.URI;
import prime.prime.infrastructure.utility.PropertiesExtractor;

public class MultipleAttendeesException extends ErrorResponseException {

    public MultipleAttendeesException(String message) {
        super(HttpStatus.BAD_REQUEST, asProblemDetail(message,
            PropertiesExtractor.getProperty("java-api.problem-definitions-url")), null);
    }

    private static ProblemDetail asProblemDetail(String message, String problemUrl) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setTitle("Multiple attendees error");
        problemDetail.setType(URI.create(problemUrl + "multiple-attendees"));
        return problemDetail;
    }
}
