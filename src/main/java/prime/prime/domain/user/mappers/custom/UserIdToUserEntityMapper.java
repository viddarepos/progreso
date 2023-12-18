package prime.prime.domain.user.mappers.custom;

import org.springframework.stereotype.Component;
import prime.prime.domain.event.models.AttendeesDto;
import prime.prime.domain.eventattendees.entity.EventAttendee;
import prime.prime.domain.user.entity.User;
import prime.prime.domain.user.service.UserService;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserIdToUserEntityMapper {
    private final UserService userService;

    public UserIdToUserEntityMapper(UserService userService) {
        this.userService = userService;
    }

    @UserMapping
    public Set<AttendeesDto> fromEventAttendees(Set<EventAttendee> attendees) {
        return attendees
                .stream()
                .map(u -> {
                    User user = u.getUser();
                    return new AttendeesDto(user.getId(), user.getFullName(), user.getAccount().getEmail(), u.isRequired());
                })
                .collect(Collectors.toSet());
    }

    @UserMapping
    public Set<User> fromUserId(Set<Long> attendees) {
        return userService.getAllById(attendees);
    }

    @UserMapping
    public User idToUserEntity(Long id){
        return userService.findUserById(id);
    }
}
