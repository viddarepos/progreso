CREATE TABLE season_aud
(
    id             BIGINT       NOT NULL,
    rev            INTEGER      NOT NULL,
    revtype        TINYINT DEFAULT NULL,
    name           VARCHAR(255) NOT NULL,
    start_date     DATE         NOT NULL,
    end_date       DATE         NOT NULL,
    duration_value INT          NOT NULL,
    duration_type  VARCHAR(255) NOT NULL,
    owner_id       BIGINT  DEFAULT NULL,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_season_aud_revinfo
        FOREIGN KEY (rev)
            REFERENCES revinfo (rev)
);