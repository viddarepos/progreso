package prime.prime.infrastructure.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.hibernate.query.SemanticException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String MAX_IMAGE_SIZE;

    @Value("${java-api.problem-definitions-url}")
    private String problemsUrl;



    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleInvalidArgument(ConstraintViolationException e) {
        List<Violation> violations = new ArrayList<>();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()
        ) {
            violations.add(new Violation(
                    violation.getPropertyPath().toString(),
                    e.getMessage(),
                    LocalDateTime.now()));
        }
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,getError(e.getMessage()));
        problemDetail.setTitle("Constraint violation");
        problemDetail.setType(URI.create(problemsUrl + "constraint-violation"));
        problemDetail.setProperty("violations",violations);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(problemDetail);
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        List<Violation> violations = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> violations.add(new Violation(error.getField(),
                error.getDefaultMessage(), LocalDateTime.now())));
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                getError(ex.getMessage()));
        problemDetail.setTitle("Method argument not valid");
        problemDetail.setType(URI.create(problemsUrl + "method-argument"));
        problemDetail.setProperty("violations", violations);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(JsonMappingException.class)
    public ResponseEntity<ProblemDetail> handleDateTimeParseExceptions(JsonMappingException e) {
        var references = e.getPath();

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,getError(e.getMessage()));
        problemDetail.setTitle("Datetime parse error");
        problemDetail.setType(URI.create(problemsUrl + "datetime-parse-error"));
        problemDetail.setProperty("field",references.get(0).getFieldName());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(problemDetail);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ProblemDetail> handleWrongPropertyValueAndLastAdminDeletion(
        RuntimeException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problemDetail.setTitle("Property reference error");
        problemDetail.setType(URI.create(problemsUrl + "property-reference-error"));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(problemDetail);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
        problemDetail.setTitle("Access denied");
        problemDetail.setType(URI.create(problemsUrl + "access-denied"));

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(problemDetail);
    }

    @ExceptionHandler(SizeLimitExceededException.class)
    public ResponseEntity<ProblemDetail> handleSizeLimitException() {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.PRECONDITION_FAILED, "Max image " +
                "size is " + MAX_IMAGE_SIZE);
        problemDetail.setTitle("Max size limit");
        problemDetail.setType(URI.create(problemsUrl + "image-size-limit"));

        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(problemDetail);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleInvalidParameterException(
            MethodArgumentTypeMismatchException e) {
        String errorMessage = "Invalid value for parameter: " + e.getName() + "." +
                " Expected type: " + e.getParameter().getParameterType().getSimpleName() +
                " Provided value: " + e.getValue();

        Violation violation = new Violation(e.getName(), errorMessage, LocalDateTime.now());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,getError(e.getMessage()));
        problemDetail.setTitle("Method argument type mismatch");
        problemDetail.setType(URI.create(problemsUrl + "method-argument-type-mismatch"));
        problemDetail.setProperty("violations",violation);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(problemDetail);
    }

    @ExceptionHandler(SemanticException.class)
    public ResponseEntity<ProblemDetail> handleInvalidParametersException(
            SemanticException e) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,getError(e.getMessage()));
        problemDetail.setTitle("Property reference error");
        problemDetail.setType(URI.create(problemsUrl + "property-reference-error"));
        problemDetail.setDetail(getModifiedErrorMessage(e.getMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(problemDetail);
    }

    private record ErrorResponse(List<?> violations) {

    }

    private record Violation(String field, String error, LocalDateTime timestamp) {

    }

    private String getError(String error) {
        if (error.contains("escape")) {
            return "Invalid character used";
        }
        return "Invalid format, please compare your request to application documentation";
    }

    private String getModifiedErrorMessage(String errorMessage) {
        String modifiedErrorMessage = errorMessage
                .replaceAll("Could not resolve attribute ", "No property ")
                .replaceAll(" of '[^']+", " for type ");

        return modifiedErrorMessage + errorMessage.substring(errorMessage.lastIndexOf(".") + 1);
    }
}
