package prime.prime.domain.user.models;

import java.time.LocalDate;
import java.util.Set;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import prime.prime.domain.account.models.AccountUpdateDto;
import prime.prime.domain.user.entity.IntegrationType;
import prime.prime.infrastructure.validation.EnumValidation;

public record UserUpdateDto(
    @Size(min = 3, max = 100, message = "Full name should be between 3 and 100 characters long")
    @Pattern(regexp = "^[A-Za-z\s]*$", message = "Invalid full name")
    String fullName,

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Past(message = "Date of birth is not correct")
    LocalDate dateOfBirth,

    @Pattern(regexp = "^\\+?\\d{9,15}$", message = "Invalid phone")
    String phoneNumber,

    @Size(min = 3, message = "Location should be at least 3 characters long")
    @Pattern(regexp = ".*")
    String location,

    @Valid
    AccountUpdateDto account,

    Set<String> technologies,

    Set<@EnumValidation(value = IntegrationType.class) String> integrations
) {

}
