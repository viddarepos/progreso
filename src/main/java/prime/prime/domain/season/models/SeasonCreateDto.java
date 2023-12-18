package prime.prime.domain.season.models;

import java.time.LocalDate;
import java.util.Set;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import prime.prime.domain.season.entity.SeasonDurationType;
import prime.prime.infrastructure.validation.EnumValidation;

public record SeasonCreateDto(
    @NotEmpty(message = "Name is mandatory")
    @Size(min = 2, max = 64, message = "Name must be between 2 and 64 characters long")
    String name,

    @NotNull(message = "Duration value is mandatory")
    @Positive(message = "Duration value must be a positive number")
    Integer durationValue,

    @NotEmpty(message = "Duration type is mandatory")
    @EnumValidation(value = SeasonDurationType.class)
    String durationType,

    @NotNull(message = "Start date is mandatory")
    @FutureOrPresent(message = "Start date cannot be in the past")
    LocalDate startDate,

    @NotNull(message = "End date is mandatory")
    @Future(message = "End date must be in the future")
    LocalDate endDate,

    @NotEmpty(message = "Technologies are mandatory")
    Set<String> technologies,

    Set<Long> mentors,

    Set<Long> interns,

    Long ownerId) {

    public SeasonCreateDto(String name,
        Integer durationValue,
        String durationType,
        LocalDate startDate,
        LocalDate endDate,
        Set<String> technologies,
        Long ownerId
    ) {
        this(name, durationValue, durationType, startDate, endDate, technologies, null, null,
            ownerId);
    }
}
