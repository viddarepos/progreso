CREATE TABLE event_aud
(
    id                       BIGINT        NOT NULL,
    rev                      INTEGER       NOT NULL,
    revtype                  TINYINT       NOT NULL,
    title                    VARCHAR(255)  NOT NULL,
    description              VARCHAR(255),
    start_time               DATETIME      NOT NULL,
    duration                 BIGINT        NOT NULL,
    creator_id               BIGINT        NOT NULL,
    google_calendar_event_id varchar(1024) NOT NULL,
    season_id                BIGINT        NOT NULL,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_event_aud_revinfo
        FOREIGN KEY (rev)
            REFERENCES revinfo (rev)
);