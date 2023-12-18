package prime.prime.domain.event.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.Length;
import prime.prime.domain.eventattendees.entity.EventAttendee;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.user.entity.User;
import prime.prime.infrastructure.audit.Auditable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Audited
public class Event extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title cannot be empty")
    @Length(min = 2, message = "Title must have minimum 2 characters")
    @Length(max = 64, message = "Title can have maximum 64 characters")
    @Column(nullable = false)
    private String title;

    @NotNull(message = "Description cannot be empty")
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @NotNull(message = "Start time cannot be empty")
    @Column(nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "Duration cannot be empty")
    @Column(nullable = false)
    private Duration duration;

    @NotNull(message = "End time cannot be empty")
    @Column(nullable = false)
    private LocalDateTime endTime;

    @ManyToOne
    private User creator;

    @Column(name = "google_calendar_event_id")
    private String googleCalendarEventId;

    @OneToMany(mappedBy = "event", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EventAttendee> eventAttendees;

    @ManyToOne
    private Season season;

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Set<EventAttendee> getEventAttendees() {
        return eventAttendees;
    }

    public void setEventAttendees(Set<EventAttendee> eventAttendees) {
        this.eventAttendees = eventAttendees;
    }

    public String getGoogleCalendarEventId() {
        return googleCalendarEventId;
    }

    public void setGoogleCalendarEventId(String googleCalendarEventId) {
        this.googleCalendarEventId = googleCalendarEventId;
    }
}
