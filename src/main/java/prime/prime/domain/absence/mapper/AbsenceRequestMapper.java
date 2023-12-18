package prime.prime.domain.absence.mapper;

import org.mapstruct.*;
import prime.prime.domain.absence.entity.AbsenceRequest;
import prime.prime.domain.absence.mapper.custom.AbsenceMapping;
import prime.prime.domain.absence.mapper.custom.DisplayNameMapper;
import prime.prime.domain.absence.models.AbsenceRequestDto;
import prime.prime.domain.absence.models.AbsenceRequestUpdateDto;
import prime.prime.domain.absence.models.AbsenceResponseDto;
import prime.prime.domain.absence.models.CalendarAbsenceResponseDto;

@Mapper(componentModel = "spring",
        uses = {DisplayNameMapper.class})
public interface AbsenceRequestMapper {

    @Mapping(target = "season", ignore = true)
    AbsenceRequest toAbsenceRequest(AbsenceRequestDto absenceRequestDto);

    @Mapping(target = "requesterId", source = "requester.id")
    @Mapping(target = "seasonId", source="season.id")
    AbsenceResponseDto toAbsenceResponse(AbsenceRequest absenceRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget AbsenceRequest absenceRequest, AbsenceRequestUpdateDto absenceRequestUpdateDto);

    @Mapping(target = "displayName", source = "absenceRequest", qualifiedBy = AbsenceMapping.class)
    CalendarAbsenceResponseDto toCalendarAbsences(AbsenceRequest absenceRequest);
}
