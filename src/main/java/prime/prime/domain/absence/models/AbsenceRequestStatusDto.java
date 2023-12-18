package prime.prime.domain.absence.models;

import prime.prime.domain.absence.entity.AbsenceRequestStatus;
import prime.prime.infrastructure.validation.EnumValidation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AbsenceRequestStatusDto(
        @NotBlank(message = "Comment cannot be empty")
        String comment,

        @NotNull(message = "Status is a required field")
        @EnumValidation(value = AbsenceRequestStatus.class, message = "Invalid value for absence request status")
        AbsenceRequestStatus status
) {
}
