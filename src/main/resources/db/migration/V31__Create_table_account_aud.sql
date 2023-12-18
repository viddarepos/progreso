CREATE TABLE account_aud
(
    id       BIGINT       NOT NULL,
    rev      INTEGER      NOT NULL,
    revtype  TINYINT      NOT NULL,
    email    VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(255) NOT NULL,
    status   VARCHAR(255) NOT NULL,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_account_aud_revinfo
        FOREIGN KEY (rev)
            REFERENCES revinfo (rev)
);