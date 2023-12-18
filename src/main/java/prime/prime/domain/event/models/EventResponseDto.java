package prime.prime.domain.event.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record EventResponseDto(
        Long id,

        String title,

        String description,

        @Schema(type = "string", pattern = "yyyy-MM-dd HH:mm", example = "2023-09-14 13:00")
        LocalDateTime startTime,

        Long duration,

        @Schema(type = "string", pattern = "yyyy-MM-dd HH:mm", example = "2023-09-14 13:00")
        LocalDateTime endTime,

        Long creatorId

) {}
