package prime.prime.domain.event.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

public record EventResponseWithAttendeesDto(
    Long id,

    String title,

    String description,

    @Schema(type = "string", pattern = "yyyy-MM-dd HH:mm", example = "2023-09-14 13:00")
    LocalDateTime startTime,

    Long duration,

    @Schema(type = "string", pattern = "yyyy-MM-dd HH:mm", example = "2023-09-14 13:00")
    LocalDateTime endTime,

    Long creatorId,

    Long seasonId,

    Set<AttendeesDto> attendees
) {

}
