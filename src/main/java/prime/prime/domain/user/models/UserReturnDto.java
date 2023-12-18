package prime.prime.domain.user.models;

import prime.prime.domain.account.models.AccountReturnDto;
import prime.prime.domain.season.models.UserSeasonDto;
import prime.prime.domain.technology.models.TechnologyReturnDto;

import java.time.LocalDate;
import java.util.Set;
import prime.prime.domain.user.entity.IntegrationType;

public record UserReturnDto(
    Long id,

    String fullName,

    LocalDate dateOfBirth,

    String phoneNumber,

    String location,

    Set<TechnologyReturnDto> technologies,

    String imagePath,

    AccountReturnDto account,

    Set<IntegrationType> integrations,

    Set<UserSeasonDto> seasons
) {

}
