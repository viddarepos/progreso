package prime.prime.domain.user.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.domain.Specification;
import prime.prime.domain.account.entity.AccountStatus;
import prime.prime.domain.role.Role;
import prime.prime.domain.user.entity.User;
import prime.prime.domain.user.models.SearchUserDto;
import prime.prime.infrastructure.security.ProgresoUserDetails;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification implements Specification<User> {

    private final SearchUserDto searchUserDto;
    private final ProgresoUserDetails userDetails;

    public UserSpecification(SearchUserDto searchUserDto, ProgresoUserDetails userDetails) {
        this.searchUserDto = searchUserDto;
        this.userDetails = userDetails;
    }

    @Override
    public Predicate toPredicate(@NotNull Root<User> root,
                                 @NotNull CriteriaQuery<?> query,
                                 @NotNull CriteriaBuilder criteriaBuilder) {

        Field[] declaredFields = searchUserDto.getClass().getDeclaredFields();

        List<Predicate> predicates = new ArrayList<>();
        List<String> groupingFields = List.of("location", "season", "technology");
        for (Field searchField : declaredFields) {
            searchField.setAccessible(true);

            String searchValue;
            try {
                searchValue = (String) searchField.get(searchUserDto);
                if (searchValue == null) {
                    continue;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            searchValue = replaceSpaces(searchValue);
            List<Predicate> orFieldPredicates = new ArrayList<>();
            if (userDetails.hasRole("ROLE_ADMIN") && groupingFields.contains(searchField.getName())) {
                String[] values = searchValue.split(",");
                for (String value : values) {
                    switch (searchField.getName()) {
                        case "location" ->
                                orFieldPredicates.add(criteriaBuilder.like(root.get("location"), '%' + value + '%'));
                        case "season" ->
                                orFieldPredicates.add(criteriaBuilder.like(root.get("seasons").get("name"), '%' + value + '%'));
                        case "technology" ->
                                orFieldPredicates.add(criteriaBuilder.like(root.get("technologies").get("name"), '%' + value + '%'));
                    }
                }
                predicates.add(criteriaBuilder.or(orFieldPredicates.toArray(new Predicate[0])));

            } else {
                switch (searchField.getName()) {
                    case "role" ->
                            predicates.add(criteriaBuilder.equal(root.get("account").get(searchField.getName()), Role.valueOf(searchValue)));
                    case "status" ->
                            predicates.add(criteriaBuilder.equal(root.get("account").get(searchField.getName()), AccountStatus.valueOf(searchValue)));
                    case "fullName" ->
                            predicates.add(criteriaBuilder.like(root.get("fullName"), '%' + searchValue + '%'));
                }
            }
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private static String replaceSpaces(String searchValue) {
        return searchValue.replace("%20", " ");
    }
}
