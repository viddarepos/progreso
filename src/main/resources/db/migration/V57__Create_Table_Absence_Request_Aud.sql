CREATE TABLE absence_request_aud
(
    id            BIGINT       NOT NULL,
    rev           INTEGER      NOT NULL,
    revtype       TINYINT      NOT NULL,
    title VARCHAR(255) NOT NULL,
    description LONGTEXT,
    status VARCHAR(30) NOT NULL,
    absence_type VARCHAR(30) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    requester_id BIGINT,
    season_id BIGINT,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_absence_request_aud_revinfo
        FOREIGN KEY (rev)
            REFERENCES revinfo (rev)
);

ALTER TABLE absence_request
    ADD COLUMN created_by         VARCHAR(255),
    ADD COLUMN created_date       TIMESTAMP,
    ADD COLUMN last_modified_by   VARCHAR(255),
    ADD COLUMN last_modified_date TIMESTAMP;