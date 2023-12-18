package prime.prime.domain.absence.mapper.custom;

import org.springframework.stereotype.Component;
import prime.prime.domain.absence.entity.AbsenceRequest;

@Component
public class DisplayNameMapper {

    @AbsenceMapping
    public String createDisplayName(AbsenceRequest absenceRequest) {
        return String.format("%s - %s", absenceRequest.getRequester().getFullName(), absenceRequest.getAbsenceType().toString());
    }
}
