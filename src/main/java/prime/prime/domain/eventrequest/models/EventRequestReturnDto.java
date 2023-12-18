package prime.prime.domain.eventrequest.models;

public record EventRequestReturnDto(
    Long id,
    String title,
    String description,
    String status,
    Long requesterId,
    Long assigneeId,
    Long seasonId
) {

    public EventRequestReturnDto(
        Long id,
        String title,
        String description,
        String status,
        Long requesterId,
        Long seasonId
    ) {
        this(id, title, description, status, requesterId, null, seasonId);
    }
}

