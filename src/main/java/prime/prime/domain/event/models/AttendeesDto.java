package prime.prime.domain.event.models;

public record AttendeesDto(
        Long id,
        String fullName,
        String email,
        boolean required
) {
}
