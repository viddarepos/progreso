package prime.prime.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.net.URI;
import prime.prime.infrastructure.utility.PropertiesExtractor;

public class NoTechnologyException extends ErrorResponseException {

    public NoTechnologyException() {
        super(HttpStatus.BAD_REQUEST,
            asProblemDetail("Mentor or intern should have at least one technology assigned to them",
                PropertiesExtractor.getProperty("java-api.problem-definitions-url")), null);
    }

    private static ProblemDetail asProblemDetail(String message, String problemUrl) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setTitle("Technology not assigned");
        problemDetail.setType(URI.create(problemUrl + "no-assigned-technology"));
        return problemDetail;
    }
}
