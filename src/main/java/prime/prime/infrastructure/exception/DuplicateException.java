package prime.prime.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.net.URI;
import prime.prime.infrastructure.utility.PropertiesExtractor;

public class DuplicateException extends ErrorResponseException {

    public DuplicateException(String className, String fieldName, String fieldValue) {
        super(HttpStatus.CONFLICT,
            asProblemDetail(className + " with " + fieldName + " " + fieldValue + " already exists",
                PropertiesExtractor.getProperty("java-api.problem-definitions-url")), null);
    }

    private static ProblemDetail asProblemDetail(String message, String problemUrl) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
        problemDetail.setTitle("Entity already exists");
        problemDetail.setType(URI.create(problemUrl + "duplicate"));
        return problemDetail;
    }

}
