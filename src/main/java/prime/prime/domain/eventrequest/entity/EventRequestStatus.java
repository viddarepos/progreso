package prime.prime.domain.eventrequest.entity;

public enum EventRequestStatus {
    REQUESTED, REJECTED, APPROVED, SCHEDULED;

    public boolean isRequested() {
        return this.equals(EventRequestStatus.REQUESTED);
    }

    public boolean isRejected() {
        return this.equals(EventRequestStatus.REJECTED);
    }

    public boolean isApproved() {
        return this.equals(EventRequestStatus.APPROVED);
    }

    public boolean isScheduled() {
        return this.equals(EventRequestStatus.SCHEDULED);
    }

    public boolean canBeChangedTo(EventRequestStatus status) {
        return (this.isRequested() && (status.isApproved() || status.isRejected()))
            || (this.isApproved() && status.isScheduled());
    }
}
