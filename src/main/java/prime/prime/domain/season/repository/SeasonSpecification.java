package prime.prime.domain.season.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.domain.Specification;
import prime.prime.domain.role.Role;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.season.models.SearchSeasonDto;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SeasonSpecification implements Specification<Season> {

    private final SearchSeasonDto seasonDto;

    public SeasonSpecification(SearchSeasonDto seasonDto) {
        this.seasonDto = seasonDto;
    }

    @Override
    public Predicate toPredicate(@NotNull Root<Season> root,
                                 @NotNull CriteriaQuery<?> query,
                                 @NotNull CriteriaBuilder criteriaBuilder) {

        List<Predicate> predicates = new ArrayList<>();

        Field[] declaredFields = seasonDto.getClass().getDeclaredFields();

        for (Field searchField : declaredFields) {

            searchField.setAccessible(true);
            String searchValue;
            try {
                if(!searchField.getName().equals("mentorId")) {
                    continue;
                }

                Object fieldValue = searchField.get(seasonDto);
                if (fieldValue == null || (fieldValue instanceof String && ((String) fieldValue).isEmpty())) {
                    continue;
                }
                searchValue = fieldValue.toString();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            String[] values = searchValue.split(",");
            Predicate[] valuePredicates = Arrays.stream(values)
                    .map(value -> {
                        Predicate rolePredicate = criteriaBuilder.equal(
                                root.get("users").get("account").get("role"), Role.MENTOR);

                        Predicate idPredicate = criteriaBuilder.equal(
                                root.get("users").get("id"), value);
                        return criteriaBuilder.and(idPredicate, rolePredicate);
                    })
                    .toArray(Predicate[]::new);
            predicates.add(criteriaBuilder.or(valuePredicates));
        }
        query.distinct(true);
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
