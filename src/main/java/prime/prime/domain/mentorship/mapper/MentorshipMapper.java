package prime.prime.domain.mentorship.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import prime.prime.domain.mentorship.entity.Mentorship;
import prime.prime.domain.mentorship.models.AssignMentorToUserDto;
import prime.prime.domain.mentorship.models.AssignMentorToUserResponseDto;
import prime.prime.domain.season.mapper.SeasonMapper;
import prime.prime.domain.season.service.SeasonService;
import prime.prime.domain.user.mappers.UserMapper;
import prime.prime.domain.user.mappers.custom.UserIdToUserEntityMapper;
import prime.prime.domain.user.mappers.custom.UserMapping;

@Mapper(componentModel = "spring",uses = {SeasonMapper.class, UserMapper.class, SeasonService.class, UserIdToUserEntityMapper.class})
public interface MentorshipMapper {

    @Mapping(source = "mentor", target = "mentor")
    @Mapping(source = "intern", target = "intern")
    @Mapping(source = "season", target = "season")
    @Mapping(source = "id", target = "id")
    AssignMentorToUserResponseDto mentorshipAssignmentToDto(Mentorship assignment);

    @Mapping(source = "mentorId", target = "mentor",qualifiedBy = UserMapping.class)
    @Mapping(source = "seasonId", target = "season")
    Mentorship dtoToMentorshipAssignment(AssignMentorToUserDto dto);
}