package prime.prime.domain.mentorship.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AssignMentorToUserDto(
        @NotNull(message = "mentorId value is mandatory")
        Long mentorId,
        @NotNull(message = "seasonId value is mandatory")
        Long seasonId,
        @NotNull(message = "Start date cannot be empty")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate startDate,
        @NotNull(message = "End date cannot be empty")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate endDate
) {
}
