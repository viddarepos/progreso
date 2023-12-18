package prime.prime.domain.absence.models;

public record SearchAbsenceRequestDto(
        String status,
        String seasonId,
        String absenceType
) {
}
