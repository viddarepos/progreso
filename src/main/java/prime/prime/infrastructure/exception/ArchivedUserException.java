package prime.prime.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.net.URI;
import prime.prime.infrastructure.utility.PropertiesExtractor;

public class ArchivedUserException extends ErrorResponseException {


    public ArchivedUserException(String className, String fieldName, Long fieldValue) {
        super(HttpStatus.NOT_FOUND,
            asProblemDetail(className + " with " + fieldName + " : " + fieldValue + " is archived",
                PropertiesExtractor.getProperty("java-api.problem-definitions-url")), null);
    }

    private static ProblemDetail asProblemDetail(String message, String problemUrl) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, message);
        problemDetail.setTitle("User is archived");
        problemDetail.setType(URI.create(problemUrl + "user-archived"));
        return problemDetail;
    }
}
