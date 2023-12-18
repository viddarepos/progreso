package prime.prime.infrastructure.password.validator;

import org.passay.*;
import prime.prime.infrastructure.password.generator.PasswordConstraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {
    @Override
    public void initialize(final ValidPassword arg0) {
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if(password == null) {
            return false;
        }

        PasswordValidator validator = new PasswordValidator(Arrays.asList(
                PasswordConstraint.LENGTH_RULE,
                PasswordConstraint.UPPER_CASE,
                PasswordConstraint.LOWER_CASE,
                PasswordConstraint.DIGIT,
                PasswordConstraint.SPECIAL_CHAR
        ));

        RuleResult result = validator.validate(new PasswordData(password));

        if (result.isValid()) {
            return true;
        }

        List<String> messages = validator.getMessages(result);

        String messageTemplate = String.join(",", messages);

        context.buildConstraintViolationWithTemplate(messageTemplate)
                .addConstraintViolation()
                .disableDefaultConstraintViolation();
        return false;
    }
}

