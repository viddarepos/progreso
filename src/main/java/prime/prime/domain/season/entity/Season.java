package prime.prime.domain.season.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.envers.Audited;
import prime.prime.domain.absence.entity.AbsenceRequest;
import prime.prime.domain.event.entity.Event;
import prime.prime.domain.eventrequest.entity.EventRequest;
import prime.prime.domain.technology.entity.Technology;
import prime.prime.domain.user.entity.User;
import prime.prime.infrastructure.audit.Auditable;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Audited
public class Season extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Name is mandatory")
    @Size(min = 2, max = 64, message = "Name must be between 2 and 64 characters long")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Duration value is mandatory")
    @Positive(message = "Duration value must be a positive number")
    @Column(nullable = false)
    private Integer durationValue;

    @NotNull(message = "Duration type is mandatory")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeasonDurationType durationType;

    @NotNull(message = "Start date is mandatory")
    @FutureOrPresent(message = "Start date cannot be in the past")
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is mandatory")
    @Future(message = "End date must be in the future")
    @Column(nullable = false)
    private LocalDate endDate;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "season_technologies",
            joinColumns = @JoinColumn(name = "season_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "technology_id", referencedColumnName = "id")
    )
    private Set<Technology> technologies;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "season_users",
            joinColumns = @JoinColumn(name = "season_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id")
    )

    private Set<User> users;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL)
    private Set<EventRequest> requests;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL)
    private Set<Event> events;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL)
    private Set<AbsenceRequest> absenceRequests;

    @ManyToOne
    private User owner;

    public Long getId() {
        return id;
    }

    public Set<AbsenceRequest> getAbsenceRequests() {
        return absenceRequests;
    }

    public void setAbsenceRequests(Set<AbsenceRequest> absenceRequests) {
        this.absenceRequests = absenceRequests;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDurationValue() {
        return durationValue;
    }

    public void setDurationValue(Integer durationValue) {
        this.durationValue = durationValue;
    }

    public SeasonDurationType getDurationType() {
        return durationType;
    }

    public void setDurationType(SeasonDurationType durationType) {
        this.durationType = durationType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Set<Technology> getTechnologies() {
        return technologies;
    }

    public void setTechnologies(Set<Technology> technologies) {
        this.technologies = technologies;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<EventRequest> getRequests() {
        return requests;
    }

    public void setRequests(Set<EventRequest> requests) {
        this.requests = requests;
    }

    public Set<Event> getEvents() {
        return events;
    }

    public void setEvents(Set<Event> events) {
        this.events = events;
    }

    public boolean isActive() {
        return getStartDate().isBefore(LocalDate.now()) && getEndDate()
                .isAfter(LocalDate.now()) || getStartDate().equals(LocalDate.now());
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
