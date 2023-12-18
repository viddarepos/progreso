package prime.prime.domain.mentorship.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import prime.prime.domain.season.entity.Season;
import prime.prime.domain.user.entity.User;

import java.time.LocalDate;

@Entity
public class Mentorship {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @ManyToOne(optional = false)
        private User intern;
        @ManyToOne(optional = false)
        private User mentor;
        @ManyToOne(optional = false)
        private Season season;
        @NotNull(message = "Start date cannot be null")
        @Column(nullable = false)
        private LocalDate startDate;
        @NotNull(message = "End date cannot be null")
        @Column(nullable = false)
        private LocalDate endDate;

        public Mentorship() {
        }
        public Mentorship(Long id, User intern, User mentor, Season season, LocalDate startDate, LocalDate endDate) {
                this.id = id;
                this.intern = intern;
                this.mentor = mentor;
                this.season = season;
                this.startDate = startDate;
                this.endDate = endDate;
        }

        public Long getId() {
                return id;
        }

        public void setId(Long id) {
                this.id = id;
        }

        public User getIntern() {
                return intern;
        }

        public void setIntern(User intern) {
                this.intern = intern;
        }

        public User getMentor() {
                return mentor;
        }

        public void setMentor(User mentor) {
                this.mentor = mentor;
        }

        public Season getSeason() {
                return season;
        }

        public void setSeason(Season season) {
                this.season = season;
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
}
