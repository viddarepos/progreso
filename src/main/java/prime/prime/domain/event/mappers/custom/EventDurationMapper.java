package prime.prime.domain.event.mappers.custom;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Component
public class EventDurationMapper {

    @EventDurationMapping
    public Long fromDuration(Duration duration) {
        return duration.toMinutes();
    }

    @EventDurationMapping
    public Duration toDuration(Long duration) {
        return Duration.of(duration, ChronoUnit.MINUTES);
    }
}
