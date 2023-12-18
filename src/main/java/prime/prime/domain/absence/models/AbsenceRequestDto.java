package prime.prime.domain.absence.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import prime.prime.domain.absence.entity.AbsenceRequestType;
import prime.prime.infrastructure.validation.EnumValidation;

import java.time.LocalDateTime;

public record AbsenceRequestDto(

        @NotBlank(message = "Title cannot be empty")
        @Length(min = 2, max = 64, message = "Title must be between 2 and 64 symbols")
        String title,

        String description,

        @NotNull(message = "Absence type is a required field")
        @EnumValidation(value = AbsenceRequestType.class, message = "Invalid value for event absence type")
        AbsenceRequestType absenceType,

        @NotNull(message = "Start time cannot be empty")
        @Future(message = "Start time must be in future")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
        @Schema(type = "string", pattern = "yyyy-MM-dd HH:mm",example = "2023-09-14 13:00")
        LocalDateTime startTime,

        @NotNull(message = "End time cannot be empty")
        @Future(message = "End time must be in future")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
        @Schema(type = "string", pattern = "yyyy-MM-dd HH:mm",example = "2023-09-14 13:00")
        LocalDateTime endTime,

        @NotNull(message = "Season cannot be null")
        Long seasonId
) {
}
