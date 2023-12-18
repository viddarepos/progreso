package prime.prime.infrastructure.password.encoder;

import org.mapstruct.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Qualifier
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface EncodedMapping {
}
