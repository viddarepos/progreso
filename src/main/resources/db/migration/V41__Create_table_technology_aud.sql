CREATE TABLE technology_aud
(
    id      BIGINT       NOT NULL,
    rev     INTEGER      NOT NULL,
    revtype TINYINT      NOT NULL,
    name    VARCHAR(100) NOT NULL,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_technology_aud_revinfo
        FOREIGN KEY (rev)
            REFERENCES revinfo (rev)
);