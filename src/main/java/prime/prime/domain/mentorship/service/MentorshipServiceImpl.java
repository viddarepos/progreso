package prime.prime.domain.mentorship.service;

import org.springframework.stereotype.Service;
import prime.prime.domain.mentorship.entity.Mentorship;
import prime.prime.domain.mentorship.mapper.MentorshipMapper;
import prime.prime.domain.mentorship.models.AssignMentorToUserDto;
import prime.prime.domain.mentorship.models.AssignMentorToUserResponseDto;
import prime.prime.domain.mentorship.repository.MentorshipRepository;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.season.service.SeasonService;
import prime.prime.domain.season.utility.SeasonUtility;
import prime.prime.domain.user.entity.User;
import prime.prime.domain.user.service.UserService;
import prime.prime.infrastructure.exception.SeasonException;
import prime.prime.infrastructure.exception.UserRoleException;

import java.time.LocalDate;

@Service
public class MentorshipServiceImpl implements MentorshipService{
    private final SeasonService seasonService;
    private final UserService userService;
    private final MentorshipMapper mentorshipMapper;
    private final MentorshipRepository mentorshipRepository;

    public MentorshipServiceImpl(SeasonService seasonService, UserService userService,
                                 MentorshipMapper mentorshipMapper, MentorshipRepository mentorshipRepository) {
        this.seasonService = seasonService;
        this.userService = userService;
        this.mentorshipMapper = mentorshipMapper;
        this.mentorshipRepository = mentorshipRepository;
    }

    @Override
    public AssignMentorToUserResponseDto assignMentor(Long id, AssignMentorToUserDto assignMentorToUserDto) {
        User userIntern = userService.findUserById(id);
        Long mentorId = assignMentorToUserDto.mentorId();
        User userMentor = userService.findUserById(mentorId);

        validateUserIsRole(userIntern, "INTERN", id.toString());
        validateUserIsRole(userMentor, "MENTOR", userMentor.getAccount().getId().toString());

        Long seasonId = assignMentorToUserDto.seasonId();
        Season season = seasonService.findById(seasonId);

        LocalDate startDate= assignMentorToUserDto.startDate();
        LocalDate endDate = assignMentorToUserDto.endDate();
        SeasonUtility.validateStartDateIsBeforeEndDate(startDate,endDate);

        if (!SeasonUtility.isWithinSeason(season, startDate.atStartOfDay(), endDate.atStartOfDay())) {
            throw new SeasonException("Provided dates are not within season");
        }
        checkIfUserIsAssignedToSeason(id, season);
        checkIfUserIsAssignedToSeason(mentorId, season);

        Mentorship mentorship = mentorshipMapper.dtoToMentorshipAssignment(assignMentorToUserDto);
        mentorship.setIntern(userIntern);
        return mentorshipMapper.mentorshipAssignmentToDto(mentorshipRepository.save(mentorship));
    }

    private void checkIfUserIsAssignedToSeason(Long id, Season season) {
        if (!SeasonUtility.isUserWithinSeason(id, season)) {
            throw new SeasonException(id, season.getId());
        }
    }

    private void validateUserIsRole(User user, String expectedRole, String idValue) {
        if (!user.getAccount().getRole().name().equals(expectedRole)) {
            throw new UserRoleException(User.class.getSimpleName(), "id", idValue, expectedRole);
        }
    }

}