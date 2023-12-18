package prime.prime.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.net.URI;
import prime.prime.infrastructure.utility.PropertiesExtractor;

public class EmailSendException extends ErrorResponseException {

    public EmailSendException(String message) {
        super(HttpStatus.PRECONDITION_FAILED, asProblemDetail(message,
            PropertiesExtractor.getProperty("java-api.problem-definitions-url")), null);
    }
    private static ProblemDetail asProblemDetail(String message, String problemUrl) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.PRECONDITION_FAILED, message);
        problemDetail.setTitle("Email error");
        problemDetail.setType(URI.create(problemUrl + "email-send-error"));
        return problemDetail;
    }
}
