package prime.prime.domain.absence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.envers.Audited;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.user.entity.User;
import prime.prime.infrastructure.audit.Auditable;

import java.time.LocalDateTime;

@Entity
@Audited
public class AbsenceRequest extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AbsenceRequestStatus status;

    @ManyToOne
    private User requester;

    @ManyToOne
    private Season season;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AbsenceRequestType absenceType;

    @NotNull(message = "Start time cannot be null")
    @Column(nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "End time cannot be null")
    @Column(nullable = false)
    private LocalDateTime endTime;

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

    public AbsenceRequestStatus getStatus() {
        return status;
    }

    public void setStatus(AbsenceRequestStatus status) {
        this.status = status;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public AbsenceRequestType getAbsenceType() {
        return absenceType;
    }

    public void setAbsenceType(AbsenceRequestType absenceType) {
        this.absenceType = absenceType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "AbsenceRequest{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", requester=" + requester +
                ", season=" + season +
                ", absenceType=" + absenceType +
                ", startDate=" + startTime +
                ", endDate=" + endTime +
                '}';
    }
}
