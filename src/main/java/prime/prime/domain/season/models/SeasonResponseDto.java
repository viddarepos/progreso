package prime.prime.domain.season.models;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import prime.prime.domain.season.entity.SeasonDurationType;
import prime.prime.domain.technology.models.TechnologyReturnDto;
import prime.prime.domain.user.models.UserReturnDto;

public record SeasonResponseDto(
    Long id,
    String name,
    Integer durationValue,
    SeasonDurationType durationType,
    LocalDate startDate,
    LocalDate endDate,
    List<TechnologyReturnDto> technologies,
    Set<UserReturnDto> mentors,
    Set<UserReturnDto> interns,
    UserReturnDto owner) {

}
