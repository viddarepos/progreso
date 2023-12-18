package prime.prime.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.net.URI;
import prime.prime.infrastructure.utility.PropertiesExtractor;

public class NotFoundException extends ErrorResponseException {

    public NotFoundException(String className, String fieldName, String fieldValue) {
        super(HttpStatus.NOT_FOUND,
            asProblemDetail(className + " with " + fieldName + " : " + fieldValue + " not found;",
                PropertiesExtractor.getProperty("java-api.problem-definitions-url")), null);
    }

    private static ProblemDetail asProblemDetail(String message, String problemUrl) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, message);
        problemDetail.setTitle("Entity Not Found");
        problemDetail.setType(URI.create(problemUrl + "not-found"));
        return problemDetail;
    }

}
