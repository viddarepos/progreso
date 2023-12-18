CREATE TABLE season_users_aud
(
    rev       int     NOT NULL,
    season_id BIGINT  NOT NULL,
    user_id   BIGINT  NOT NULL,
    revtype   TINYINT NOT NULL,
    PRIMARY KEY (rev, season_id, user_id),
    CONSTRAINT season_users_aud_revinfo
        FOREIGN KEY (rev)
            REFERENCES revinfo (rev)
);