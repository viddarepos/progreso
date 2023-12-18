package prime.prime.domain.user.entity;

import java.time.LocalDate;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.springframework.format.annotation.DateTimeFormat;
import prime.prime.domain.account.entity.Account;
import prime.prime.domain.account.entity.AccountStatus;
import prime.prime.domain.event.entity.Event;
import prime.prime.domain.eventattendees.entity.EventAttendee;
import prime.prime.domain.eventrequest.entity.EventRequest;
import prime.prime.domain.role.Role;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.technology.entity.Technology;
import prime.prime.infrastructure.audit.Auditable;

@Entity
@Audited
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Full name is mandatory")
    @Size(min = 3, max = 100, message = "Full name should be between 3 and 100 characters long")
    @Pattern(regexp = "^[A-Za-z\s]*$", message = "Invalid full name")
    private String fullName;

    @Column(nullable = false)
    @NotNull(message = "Date of birth is mandatory")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Past(message = "Date of birth is not correct")
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    @NotBlank(message = "Phone number is mandatory")
    @Pattern(regexp = "^\\+?\\d{9,15}$", message = "Invalid phone")
    private String phoneNumber;

    @Column(nullable = false)
    @NotBlank(message = "Location is mandatory")
    @Size(min = 3, message = "Location should be at least 3 characters long")
    @Pattern(regexp = ".*")
    private String location;

    @NotNull(message = "Account is mandatory")
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private Account account;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "technology_users",
        joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "technology_id", referencedColumnName = "id")
    )
    private Set<Technology> technologies;

    @OneToMany(
        cascade = CascadeType.REMOVE,
        mappedBy = "creator")
    private Set<Event> event;

    @Column(name = "image_path")
    private String imagePath;

    @OneToMany(
        mappedBy = "requester",
        cascade = {CascadeType.REMOVE}
    )
    private Set<EventRequest> requests;

    @OneToMany(
        mappedBy = "assignee",
        cascade = {CascadeType.REMOVE}
    )
    private Set<EventRequest> assignedRequests;

    @OneToMany(mappedBy = "user")
    private Set<EventAttendee> eventAttendees;

    @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
    private Set<Season> seasons;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "users_integrations", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "integration_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<IntegrationType> integrations;

    @OneToMany(mappedBy = "owner",  fetch = FetchType.EAGER)
    private Set<Season> seasonsOwner;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Set<Technology> getTechnologies() {
        return technologies;
    }

    public void setTechnologies(Set<Technology> technologies) {
        this.technologies = technologies;
    }

    public Set<Event> getEvent() {
        return event;
    }

    public void setEvent(Set<Event> event) {
        this.event = event;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Set<EventRequest> getRequests() {
        return requests;
    }

    public void setRequests(Set<EventRequest> requests) {
        this.requests = requests;
    }

    public Set<EventAttendee> getEventAttendees() {
        return eventAttendees;
    }

    public void setEventAttendees(Set<EventAttendee> eventAttendees) {
        this.eventAttendees = eventAttendees;
    }

    public Set<EventRequest> getAssignedRequests() {
        return assignedRequests;
    }

    public void setAssignedRequests(Set<EventRequest> assignedRequests) {
        this.assignedRequests = assignedRequests;
    }

    public Set<Season> getSeasons() {
        return seasons;
    }

    public void setSeasons(Set<Season> seasons) {
        this.seasons = seasons;
    }

    public boolean isMentor() {
        return getAccount().getRole().equals(Role.MENTOR);
    }

    public boolean isAdmin() {
        return getAccount().getRole().equals(Role.ADMIN);
    }

    public boolean isIntern() {
        return getAccount().getRole().equals(Role.INTERN);
    }

    public Set<IntegrationType> getIntegrations() {
        return integrations;
    }

    public void setIntegrations(Set<IntegrationType> integrations) {
        this.integrations = integrations;
    }

    public boolean hasIntegration(IntegrationType integrationType) {
        if (integrations == null) {
            return false;
        }
        return integrations.contains(integrationType);
    }

    public Set<Season> getSeasonsOwner() {
        return seasonsOwner;
    }

    public void setSeasonsOwner(Set<Season> seasonsOwner) {
        this.seasonsOwner = seasonsOwner;
    }

    public boolean isActive() {
        return account.getStatus().equals(AccountStatus.ACTIVE);
    }

    public boolean isDefaultAdmin() {
        return account.getEmail().equals("admin@xprmnt.xyz");
    }
}
