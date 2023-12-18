package prime.prime.domain.absence.models;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record CalendarAbsenceResponseDto(
        Long id,
        String displayName,
        String status,
        String absenceType,
        @Schema(type = "string", pattern = "yyyy-MM-dd HH:mm", example = "2023-09-14 13:00")
        LocalDateTime startTime,
        @Schema(type = "string", pattern = "yyyy-MM-dd HH:mm", example = "2023-09-14 13:00")
        LocalDateTime endTime
) {
}
