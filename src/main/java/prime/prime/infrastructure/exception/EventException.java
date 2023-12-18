package prime.prime.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.net.URI;
import prime.prime.infrastructure.utility.PropertiesExtractor;

public class EventException extends ErrorResponseException {

    public EventException(String message) {
        super(HttpStatus.CONFLICT, asProblemDetail(message,
            PropertiesExtractor.getProperty("java-api.problem-definitions-url")), null);
    }

    private static ProblemDetail asProblemDetail(String message, String problemUrl) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Event error");
        problemDetail.setType(URI.create(problemUrl + "event-error"));
        return problemDetail;
    }

}
