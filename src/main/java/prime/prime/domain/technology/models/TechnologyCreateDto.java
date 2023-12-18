package prime.prime.domain.technology.models;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TechnologyCreateDto(
        @NotBlank(message = "Name is mandatory")
        @Column(unique = true)
        @Size(max = 100, message = "Name must be no more than 100 characters.")
        String name
){
}
