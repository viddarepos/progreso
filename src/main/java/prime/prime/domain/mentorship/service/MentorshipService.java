package prime.prime.domain.mentorship.service;

import prime.prime.domain.mentorship.models.AssignMentorToUserDto;
import prime.prime.domain.mentorship.models.AssignMentorToUserResponseDto;

public interface MentorshipService {
    AssignMentorToUserResponseDto assignMentor(Long id, AssignMentorToUserDto assignMentorToUserDto);
}
