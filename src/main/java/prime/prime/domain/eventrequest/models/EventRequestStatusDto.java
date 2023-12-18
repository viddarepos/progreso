package prime.prime.domain.eventrequest.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import prime.prime.domain.eventrequest.entity.EventRequestStatus;
import prime.prime.infrastructure.validation.EnumValidation;

public record EventRequestStatusDto(
    @NotBlank(message = "Comment is a required field!")
    String comment,

    @NotNull(message = "Status is a required field!")
    @EnumValidation(value = EventRequestStatus.class, message = "Invalid value for event request status!")
    EventRequestStatus status,

    Long assignee
) {
}
