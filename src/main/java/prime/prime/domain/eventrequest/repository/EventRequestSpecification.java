package prime.prime.domain.eventrequest.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.domain.Specification;
import prime.prime.domain.eventrequest.entity.EventRequest;
import prime.prime.domain.eventrequest.entity.EventRequestStatus;
import prime.prime.domain.eventrequest.models.SearchEventRequestDto;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventRequestSpecification implements Specification<EventRequest> {

    private final SearchEventRequestDto searchEventRequestDto;

    private final List<Long> seasons;

    public EventRequestSpecification(SearchEventRequestDto searchEventRequestDto, List<Long> seasons) {
        this.searchEventRequestDto = searchEventRequestDto;
        this.seasons = seasons;
    }

    @Override
    public Predicate toPredicate(@NotNull Root<EventRequest> root,
                                 @NotNull CriteriaQuery<?> query,
                                 @NotNull CriteriaBuilder criteriaBuilder) {

        List<Predicate> predicates = new ArrayList<>();

        Field[] declaredFields = searchEventRequestDto.getClass().getDeclaredFields();

        for (Field searchField : declaredFields) {
            searchField.setAccessible(true);

            String searchValue;
            try {
                Object fieldValue = searchField.get(searchEventRequestDto);
                if(fieldValue == null || (fieldValue instanceof String && ((String) fieldValue).isEmpty())) {
                    continue;
                }

                if(searchField.getName().equals("status") && !isStatusValid(fieldValue.toString())) {
                    return criteriaBuilder.disjunction();
                }
                searchValue = fieldValue.toString();

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if (searchField.getName().equals("status")) {
                predicates.add(criteriaBuilder.equal(
                        root.get("status"),
                    EventRequestStatus.valueOf(searchValue.toUpperCase(Locale.ROOT))));
            } else if (searchField.getName().equals("seasonId")) {
                predicates.add(criteriaBuilder.equal(
                    root.get("season").get("id"), searchEventRequestDto.seasonId()));
            }
        }
        predicates.add(root.get("season").get("id").in(seasons));

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private boolean isStatusValid(String givenStatus) {
        for (EventRequestStatus status : EventRequestStatus.values()) {
            if (status.name().equals(givenStatus.toUpperCase())) {
                return true;
            }
        }

        return false;
    }
}