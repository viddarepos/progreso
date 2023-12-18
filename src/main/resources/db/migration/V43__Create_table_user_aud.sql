CREATE TABLE user_aud
(
    id            BIGINT       NOT NULL,
    rev           INTEGER      NOT NULL,
    revtype       TINYINT      NOT NULL,
    date_of_birth DATE         NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    location      VARCHAR(255) NOT NULL,
    phone_number  VARCHAR(255) NOT NULL,
    account_id    BIGINT       NOT NULL,
    image_path    VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_user_aud_revinfo
        FOREIGN KEY (rev)
            REFERENCES revinfo (rev)
);