package prime.prime.domain.event.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

public record EventRequestDto(

    @NotBlank(message = "Title cannot be empty")
    @Length(min = 2, message = "Title must have minimum 2 characters")
    @Length(max = 64, message = "Title can have maximum 64 characters")
    String title,

    @NotNull(message = "Description cannot be null")
    @Size(max = 512, message = "Description must be no more than 512 characters.")
    String description,

    @NotNull(message = "Start time cannot be empty")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    @Schema(type = "string", pattern = "yyyy-MM-dd HH:mm",example = "2023-09-14 13:00")
    LocalDateTime startTime,

    @NotNull(message = "Duration cannot be empty")
    @Positive(message = "Duration cannot be negative or 0!")
    Long duration,

    @NotNull(message = "seasonId cannot be null")
    Long seasonId,

    @NotNull(message = "Optional attendees cannot be null")
    Set<Long> optionalAttendees,

    @NotNull(message = "Required attendees cannot be null")
    Set<Long> requiredAttendees
) {

}
