package prime.prime.domain.mentorship.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import prime.prime.domain.season.models.SeasonResponseDto;
import prime.prime.domain.user.models.UserReturnDto;

import java.time.LocalDate;

public record AssignMentorToUserResponseDto(
        Long id,
        UserReturnDto mentor,
        UserReturnDto intern,
        SeasonResponseDto season,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate startDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate endDate
) {
}
