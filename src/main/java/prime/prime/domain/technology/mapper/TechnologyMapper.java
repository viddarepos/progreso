package prime.prime.domain.technology.mapper;

import prime.prime.domain.technology.entity.Technology;
import prime.prime.domain.technology.models.TechnologyCreateDto;
import prime.prime.domain.technology.models.TechnologyReturnDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TechnologyMapper {
    Technology createDtoToTechnology(TechnologyCreateDto technologyCreateDto);
    TechnologyReturnDto technologyToReturnDto(Technology technology);
}
