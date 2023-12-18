package prime.prime.domain.season.models;

import java.time.LocalDate;
import java.util.Set;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import prime.prime.domain.season.entity.SeasonDurationType;
import prime.prime.infrastructure.validation.EnumValidation;

public record SeasonUpdateDto(
    @Size(min = 2, max = 64, message = "Name must be between 2 and 64 characters long")
    String name,

    @Positive(message = "Duration value must be a positive number")
    Integer durationValue,

    @EnumValidation(value = SeasonDurationType.class)
    String durationType,

    @FutureOrPresent(message = "Start date cannot be in the past")
    LocalDate startDate,

    @FutureOrPresent(message = "End date must be in the future")
    LocalDate endDate,

    Set<String> technologies,

    Set<Long> mentors,

    Set<Long> interns,

    @NotNull(message = "Owner id cannot be null")
    Long ownerId) {

    public SeasonUpdateDto(String name,
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
