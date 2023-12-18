package prime.prime.infrastructure.exception;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;
import prime.prime.infrastructure.utility.PropertiesExtractor;

public class AbsenceException extends ErrorResponseException {

    public AbsenceException(String message) {
        super(HttpStatus.CONFLICT, asProblemDetail(message,
            PropertiesExtractor.getProperty("java-api.problem-definitions-url")), null);
    }

    private static ProblemDetail asProblemDetail(String message, String problemUrl) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Absence error");
        problemDetail.setType(URI.create(problemUrl + "absence-error"));
        return problemDetail;
    }
}
