package prime.prime.infrastructure.validation;

import java.io.Serializable;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class EnumValidator implements ConstraintValidator<EnumValidation, Serializable> {
    private List<String> acceptedValues;
    private String message;

    @Override
    public void initialize(EnumValidation annotation) {
        acceptedValues = Stream.of(annotation.value().getEnumConstants())
                .map(Enum::name)
                .toList();

        message = "must be any of " + Arrays.toString(acceptedValues.toArray());
    }

    @Override
    public boolean isValid(Serializable value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();

        return acceptedValues.contains(value.toString());
    }
}
