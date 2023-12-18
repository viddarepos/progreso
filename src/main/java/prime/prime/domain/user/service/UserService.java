package prime.prime.domain.user.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import prime.prime.domain.account.models.AccountEmailDto;
import prime.prime.domain.account.models.ChangePasswordDto;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.user.entity.User;
import prime.prime.domain.user.models.SearchUserDto;
import prime.prime.domain.user.models.UserCreateDto;
import prime.prime.domain.user.models.UserReturnDto;
import prime.prime.domain.user.models.UserUpdateDto;
import prime.prime.domain.user.repository.Projection.UserProjection;
import prime.prime.infrastructure.security.ProgresoUserDetails;

import java.io.IOException;
import java.util.Set;

public interface UserService {

    User getEntityById(Long id);

    UserReturnDto create(UserCreateDto userCreateDto);

    UserReturnDto getById(Long id);

    Page<UserReturnDto> getAll(SearchUserDto searchUserDto, Pageable pageable, ProgresoUserDetails progresoUserDetails);

    UserReturnDto update(Long id, UserUpdateDto userUpdateDto, MultipartFile multipartFile)
        throws IOException;

    AccountEmailDto delete(Long id) throws IOException;

    AccountEmailDto changePassword(Long id, ChangePasswordDto changePasswordDto,
        HttpServletRequest httpServletRequest);

    UserProjection findUserByAccountEmail(String email);

    Set<User> getAllById(Set<Long> users);

    void deleteUserImage(Long id) throws IOException;

    AccountEmailDto archive(Long id);

    Set<User> getAllAdmins();

    User findByEmail(String email);

    boolean checkIfUserIsNotAssignedToSeason(User user, Season season);

    User findUserById(Long id);
}
