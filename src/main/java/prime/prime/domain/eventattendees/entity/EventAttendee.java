package prime.prime.domain.eventattendees.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.envers.Audited;
import prime.prime.domain.event.entity.Event;
import prime.prime.domain.user.entity.User;
import prime.prime.infrastructure.audit.Auditable;

@Entity
@Table(name = "events_attendees")
@Audited
public class EventAttendee extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean required;

    @ManyToOne
    private Event event;

    @ManyToOne
    private User user;

    public EventAttendee() {
    }

    public EventAttendee(boolean required, Event event, User user) {
        this.required = required;
        this.event = event;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isAdmin() {
        return this.getUser().isAdmin();
    }

}
