package prime.prime.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;
import prime.prime.domain.account.entity.AccountStatus;
import prime.prime.domain.role.Role;

import java.net.URI;
import prime.prime.infrastructure.utility.PropertiesExtractor;

public class SeasonException extends ErrorResponseException {


    public SeasonException(String className, String fieldName, String fieldValue, int limit) {
        super(HttpStatus.BAD_REQUEST, asProblemDetail(className + " with " + fieldName + " : " + fieldValue + " have " +
                "already been assigned to " + limit + " seasons!",
            PropertiesExtractor.getProperty("java-api.problem-definitions-url")), null);
    }

    public SeasonException(String className, String fieldName, String fieldValue, Role role) {
        super(HttpStatus.BAD_REQUEST, asProblemDetail(className + " with " + fieldName + " : " + fieldValue + " doesn" +
                "'t have the role " + role.name(), PropertiesExtractor.getProperty("java-api.problem-definitions-url")), null);
    }

    public SeasonException(String className, String fieldName, String fieldValue) {
        super(HttpStatus.BAD_REQUEST,
                asProblemDetail("Account of " + className + " with " + fieldName + " : " + fieldValue + " is not " +
                        AccountStatus.ACTIVE.name(), PropertiesExtractor.getProperty("java-api.problem-definitions-url")), null);
    }

    public SeasonException(Long fieldValue, Long seasonId) {
        super(HttpStatus.BAD_REQUEST, asProblemDetail("User with id " + fieldValue + " is not assigned to season with" +
                " id " + seasonId, PropertiesExtractor.getProperty("java-api.problem-definitions-url")), null);
    }

    public SeasonException(String message) {
        super(HttpStatus.BAD_REQUEST, asProblemDetail(message, PropertiesExtractor.getProperty("java-api.problem-definitions-url")), null);
    }

    private static ProblemDetail asProblemDetail(String message, String problemUrl) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setTitle("Season error");
        problemDetail.setType(URI.create(problemUrl + "season-error"));
        return problemDetail;
    }
}