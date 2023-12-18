CREATE TABLE technology_users_aud
(
    rev           INT     NOT NULL,
    user_id       BIGINT  NOT NULL,
    technology_id BIGINT  NOT NULL,
    revtype       TINYINT NOT NULL,
    PRIMARY KEY (rev, user_id, technology_id),
    CONSTRAINT technology_users_aud_revinfo
        FOREIGN KEY (rev)
            REFERENCES revinfo (rev)
);