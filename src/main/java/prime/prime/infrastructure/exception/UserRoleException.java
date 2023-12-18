package prime.prime.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;
import prime.prime.infrastructure.utility.PropertiesExtractor;

import java.net.URI;

public class UserRoleException extends ErrorResponseException {

    public UserRoleException(String className, String fieldName, String fieldValue,String role) {
        super(HttpStatus.BAD_REQUEST,
                asProblemDetail(className + " with " + fieldName + " : " + fieldValue + " doesn't have a role " + role,
                        PropertiesExtractor.getProperty("java-api.problem-definitions-url")), null);
    }

    private static ProblemDetail asProblemDetail(String message, String problemUrl) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setTitle("User role error");
        problemDetail.setType(URI.create(problemUrl + "user-role-error"));
        return problemDetail;
    }
}
