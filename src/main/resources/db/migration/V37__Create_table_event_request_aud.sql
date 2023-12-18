CREATE TABLE event_request_aud
(
    id           BIGINT       NOT NULL,
    rev          INTEGER      NOT NULL,
    revtype      TINYINT      NOT NULL,
    title        VARCHAR(255) NOT NULL,
    description  VARCHAR(512) NOT NULL,
    requester_id BIGINT       NOT NULL,
    status       VARCHAR(30)  NOT NULL,
    assignee_id  BIGINT       NOT NULL,
    season_id    BIGINT       NOT NULL,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_event_request_aud_revinfo
        FOREIGN KEY (rev)
            REFERENCES revinfo (rev)
);