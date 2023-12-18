package prime.prime.domain.eventrequest.models;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

public record EventRequestUpdateDto(
        @Length(min = 2, max = 64, message = "Title must be between 2 and 64 symbols")
        String title,

        @Size(max = 512, message = "Description must be no more than 512 characters.")
        String description
) {
}
