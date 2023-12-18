package prime.prime.domain.season.mapper;

import java.time.LocalDate;
import org.mapstruct.BeanMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import prime.prime.domain.role.Role;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.season.models.SeasonCreateDto;
import prime.prime.domain.season.models.SeasonResponseDto;
import prime.prime.domain.season.models.SeasonUpdateDto;
import prime.prime.domain.season.utility.SeasonUtility;
import prime.prime.domain.technology.mapper.TechnologyMapping;
import prime.prime.domain.technology.mapper.TechnologyNameToTechnologyEntity;
import prime.prime.domain.user.mappers.UserMapper;

@Mapper(componentModel = "spring",
    uses = TechnologyNameToTechnologyEntity.class,
    imports = {Role.class, SeasonUtility.class})
public interface SeasonMapper {

  UserMapper userMapper = Mappers.getMapper(UserMapper.class);

  @Mapping(target = "technologies", qualifiedBy = TechnologyMapping.class)
  @Mapping(target = "users", ignore = true)
  Season fromCreateDto(SeasonCreateDto seasonCreateDto);

  @Mapping(target = "mentors", expression = "java(userMapper.userSetToReturnDtoSet(SeasonUtility.filterAssigneesByRole(season, Role.MENTOR)))")
  @Mapping(target = "interns", expression = "java(userMapper.userSetToReturnDtoSet(SeasonUtility.filterAssigneesByRole(season, Role.INTERN)))")
  SeasonResponseDto toResponseDto(Season season);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "technologies", ignore = true)
  @Mapping(target = "users", ignore = true)
  void updateFromUpdateDto(SeasonUpdateDto seasonUpdateDto, @MappingTarget Season season);

  @BeforeMapping
  default void validateCreateDto(SeasonCreateDto seasonCreateDto,
      @MappingTarget Season season) {
    SeasonUtility.validateStartDateIsBeforeEndDate(seasonCreateDto.startDate(),
        seasonCreateDto.endDate());
  }

  @BeforeMapping
  default void validateUpdateDto(SeasonUpdateDto seasonUpdateDto,
      @MappingTarget Season season) {
    LocalDate startDate =
        seasonUpdateDto.startDate() != null ? seasonUpdateDto.startDate() : season.getStartDate();
    LocalDate endDate =
        seasonUpdateDto.endDate() != null ? seasonUpdateDto.endDate() : season.getEndDate();

    SeasonUtility.validateStartDateIsBeforeEndDate(startDate, endDate);
  }
}
