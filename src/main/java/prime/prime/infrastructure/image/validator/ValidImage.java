package prime.prime.infrastructure.image.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ImageValidator.class})
public @interface ValidImage {
    String message() default "Only PNG, JPG, JPEG or HEIC images are allowed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
