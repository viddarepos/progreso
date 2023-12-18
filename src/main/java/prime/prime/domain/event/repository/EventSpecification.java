package prime.prime.domain.event.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.domain.Specification;
import prime.prime.domain.event.entity.Event;
import prime.prime.domain.event.models.SearchEventDto;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class EventSpecification implements Specification<Event> {

    private final SearchEventDto searchEventDto;


    public EventSpecification(SearchEventDto searchEventDto) {
        this.searchEventDto = searchEventDto;
    }

    @Override
    public Predicate toPredicate(@NotNull Root<Event> root,
                                 @NotNull CriteriaQuery<?> query,
                                 @NotNull CriteriaBuilder criteriaBuilder) {

        List<Predicate> predicates = new ArrayList<>();

        Field[] declaredFields = searchEventDto.getClass().getDeclaredFields();

        for (Field searchField : declaredFields) {
            searchField.setAccessible(true);

            try {
                Object fieldValue = searchField.get(searchEventDto);
                if (fieldValue == null) {
                    continue;
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if(searchField.getName().equals("seasonId")) {
                predicates.add(criteriaBuilder.equal(
                        root.get("season").get("id"), searchEventDto.seasonId()));
            }
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}