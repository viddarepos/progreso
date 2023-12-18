package prime.prime.domain.eventrequest.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import prime.prime.domain.eventrequest.entity.EventRequest;
import prime.prime.domain.eventrequest.models.EventRequestCreateDto;
import prime.prime.domain.eventrequest.models.EventRequestReturnDto;
import prime.prime.domain.eventrequest.models.EventRequestUpdateDto;

@Mapper(componentModel = "spring")
public interface EventRequestMapper {

    @Mapping(target = "season", ignore = true)
    EventRequest toEventRequest(EventRequestCreateDto eventRequestCreateDto);

    @Mapping(target = "requesterId", source = "requester.id")
    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "seasonId", source="season.id")
    EventRequestReturnDto fromEventRequest(EventRequest eventRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "assignee", ignore = true)
    void update(@MappingTarget EventRequest eventRequest, EventRequestUpdateDto eventRequestUpdateDto);
}
