package prime.prime.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.net.URI;
import prime.prime.infrastructure.utility.PropertiesExtractor;

public class InvalidDateException extends ErrorResponseException {

    public InvalidDateException(String message) {
        super(HttpStatus.BAD_REQUEST, asProblemDetail(message, PropertiesExtractor.getProperty("java-api.problem-definitions-url")), null);
    }

    private static ProblemDetail asProblemDetail(String message, String problemUrl) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setTitle("Invalid date");
        problemDetail.setType(URI.create(problemUrl + "invalid-date"));
        return problemDetail;
    }
}
