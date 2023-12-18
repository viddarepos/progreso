package prime.prime.domain.user.models;

import org.springframework.format.annotation.DateTimeFormat;
import prime.prime.domain.account.models.AccountCreateDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

public record UserCreateDto(
        @NotBlank(message = "Full name is mandatory")
        @Size(min = 3, max = 100, message = "Full name should be between 3 and 100 characters long")
        @Pattern(regexp="^[A-Za-z\s]*$", message = "Invalid full name")
        String fullName,

        @NotNull(message = "Date of birth is mandatory")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        @Past(message = "Date of birth is not correct")
        LocalDate dateOfBirth,

        @NotBlank(message = "Phone number is mandatory")
        @Pattern(regexp ="^\\+?\\d{9,15}$", message = "Invalid phone")
        String phoneNumber,

        @NotBlank(message = "Location is mandatory")
        @Size(min = 3, message = "Location should be at least 3 characters long")
        @Pattern(regexp = ".*")
        String location,

        @NotNull(message = "Account is mandatory")
        @Valid
        AccountCreateDto account,

        @NotNull(message = "Technologies are mandatory")
        Set<String> technologies
) {
}
