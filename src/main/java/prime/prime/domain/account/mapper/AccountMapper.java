package prime.prime.domain.account.mapper;

import org.mapstruct.*;
import prime.prime.domain.account.entity.Account;
import prime.prime.domain.account.models.AccountReturnWithPasswordDto;
import prime.prime.domain.account.models.AccountUpdateDto;
import prime.prime.domain.user.repository.Projection.UserProjection;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "id", source = "accountId")
    @Mapping(target = "email", source = "accountEmail")
    @Mapping(target = "password", source = "accountPassword")
    @Mapping(target = "role", source = "accountRole")
    @Mapping(target = "status", source = "accountStatus")
    AccountReturnWithPasswordDto accountToReturnWithPasswordDto(UserProjection userProjection);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(AccountUpdateDto accountUpdateDto, @MappingTarget Account account);

   Account update(String password, @MappingTarget Account accountDb);
}
