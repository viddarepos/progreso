package prime.prime.domain.absence.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record AbsenceResponseDto(
        Long id,
        String title,
        String description,
        String status,
        Long requesterId,
        String absenceType,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        @Schema(type = "string", pattern = "yyyy-MM-dd HH:mm", example = "2023-09-14 13:00")
        LocalDateTime startTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        @Schema(type = "string", pattern = "yyyy-MM-dd HH:mm", example = "2023-09-14 13:00")
        LocalDateTime endTime,
        Long seasonId
) {
}
