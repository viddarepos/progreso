package prime.prime.domain.absence.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.domain.Specification;
import prime.prime.domain.absence.entity.AbsenceRequest;
import prime.prime.domain.absence.entity.AbsenceRequestStatus;
import prime.prime.domain.absence.entity.AbsenceRequestType;
import prime.prime.domain.absence.models.SearchAbsenceRequestDto;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AbsenceRequestSpecification implements Specification<AbsenceRequest> {

    private final SearchAbsenceRequestDto searchAbsenceRequestDto;

    private final List<Long> seasons;

    public AbsenceRequestSpecification(SearchAbsenceRequestDto searchAbsenceRequestDto, List<Long> seasons) {
        this.searchAbsenceRequestDto = searchAbsenceRequestDto;
        this.seasons = seasons;
    }

    @Override
    public Predicate toPredicate(@NotNull Root<AbsenceRequest> root,
                                 @NotNull CriteriaQuery<?> query,
                                 @NotNull CriteriaBuilder criteriaBuilder) {

        List<Predicate> predicates = new ArrayList<>();

        Field[] declaredFields = searchAbsenceRequestDto.getClass().getDeclaredFields();

        for (Field searchField : declaredFields) {
            searchField.setAccessible(true);

            String searchValue;
            try {
                Object fieldValue = searchField.get(searchAbsenceRequestDto);

                if (fieldValue == null || (fieldValue instanceof String && ((String) fieldValue).isEmpty())) {
                    continue;
                }

                if(searchField.getName().equals("status") && !isStatusValid(fieldValue.toString()) ||
                        (searchField.getName().equals("absenceType") && !isAbsenceTypeValid(fieldValue.toString()))) {
                    return criteriaBuilder.disjunction();
                }
                searchValue = fieldValue.toString();

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            switch (searchField.getName()) {
                case "status" -> predicates.add(criteriaBuilder.equal(
                        root.get("status"),
                        AbsenceRequestStatus.valueOf(searchValue.toUpperCase(Locale.ROOT))));
                case "seasonId" -> predicates.add(criteriaBuilder.equal(
                        root.get("season").get("id"),
                        searchAbsenceRequestDto.seasonId()));
                case "absenceType" -> predicates.add(criteriaBuilder.equal(
                        root.get("absenceType"),
                        AbsenceRequestType.valueOf(searchValue.toUpperCase(Locale.ROOT))));
            }
        }
        predicates.add(root.get("season").get("id").in(seasons));

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private boolean isStatusValid(String givenStatus) {
        for (AbsenceRequestStatus status : AbsenceRequestStatus.values()) {
            if (status.name().equals(givenStatus.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean isAbsenceTypeValid(String givenType) {
        for (AbsenceRequestType type : AbsenceRequestType.values()) {
            if (type.name().equals(givenType.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}
