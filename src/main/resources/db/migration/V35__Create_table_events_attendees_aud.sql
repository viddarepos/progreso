CREATE TABLE events_attendees_aud
(
    event_id BIGINT  NOT NULL,
    user_id  BIGINT  NOT NULL,
    rev      INTEGER NOT NULL,
    revtype  TINYINT NOT NULL,
    required BOOLEAN DEFAULT 0,
    id       BIGINT  NOT NULL,
    PRIMARY KEY (event_id, user_id, rev),
    CONSTRAINT fk_events_attendees_aud_revinfo
        FOREIGN KEY (rev)
            REFERENCES revinfo (rev)
);