package prime.prime.domain.user.mappers;

import java.util.Set;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import prime.prime.domain.account.mapper.AccountMapper;
import prime.prime.domain.season.mapper.SeasonMapper;
import prime.prime.domain.technology.mapper.TechnologyMapping;
import prime.prime.domain.technology.mapper.TechnologyNameToTechnologyEntity;
import prime.prime.domain.user.entity.User;
import prime.prime.domain.user.models.UserCreateDto;
import prime.prime.domain.user.models.UserReturnDto;
import prime.prime.domain.user.models.UserUpdateDto;

@Mapper(componentModel = "spring", uses = {AccountMapper.class,
    TechnologyNameToTechnologyEntity.class, SeasonMapper.class})
public interface UserMapper {

  @Mapping(target = "technologies", qualifiedBy = TechnologyMapping.class)
  User createDtoToUser(UserCreateDto userCreateDto);

  UserReturnDto userToReturnDto(User user);

  Set<UserReturnDto> userSetToReturnDtoSet(Set<User> users);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "technologies", ignore = true)
  void update(UserUpdateDto userUpdateDto, @MappingTarget User user);

}
