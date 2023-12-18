CREATE TABLE season_technologies_aud
(
    rev           INTEGER NOT NULL,
    season_id     BIGINT  NOT NULL,
    technology_id BIGINT  NOT NULL,
    revtype       TINYINT NOT NULL,
    PRIMARY KEY (rev, season_id, technology_id),
    CONSTRAINT season_technologies_aud_revinfo
        FOREIGN KEY (rev)
            REFERENCES revinfo (rev)
);