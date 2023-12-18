CREATE TABLE users_integrations_aud
(
    rev              INTEGER      NOT NULL,
    user_id          BIGINT       NOT NULL,
    integration_type VARCHAR(255) NOT NULL,
    revtype          TINYINT      NOT NULL,
    PRIMARY KEY (rev, user_id, integration_type),
    CONSTRAINT users_integrations_aud_revinfo
        FOREIGN KEY (rev)
            REFERENCES revinfo (rev)
);