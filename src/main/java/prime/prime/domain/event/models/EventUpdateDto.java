package prime.prime.domain.event.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.Set;

public record EventUpdateDto(
        @Length(min = 2, message = "Title must have minimum 2 characters")
        @Length(max = 64, message = "Title can have maximum 64 characters")
        String title,

        @Size(max = 512, message = "Description must be no more than 512 characters.")
        String description,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
        @Schema(type = "string", pattern = "yyyy-MM-dd HH:mm",example = "2023-09-14 13:00")
        LocalDateTime startTime,

        @Positive(message = "Duration cannot be negative or 0!")
        Long duration,

        boolean notifyAttendees,

        Set<Long> optionalAttendees,

        Set<Long> requiredAttendees
) {
}
