package prime.prime.domain.absence.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

public record AbsenceRequestUpdateDto(

        @Length(min = 2, max = 64, message = "Title must be between 2 and 64 symbols")
        String title,

        String description,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
        @FutureOrPresent(message = "Start time cannot be in the past")
        @Schema(type = "string", pattern = "yyyy-MM-dd HH:mm",example = "2023-09-14 13:00")
        LocalDateTime startTime,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
        @FutureOrPresent(message = "End time cannot be in the past")
        @Schema(type = "string", pattern = "yyyy-MM-dd HH:mm",example = "2023-09-14 13:00")
        LocalDateTime endTime
) {
}
