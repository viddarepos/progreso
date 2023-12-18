CREATE TABLE account (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY (email)
);

CREATE TABLE user
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    date_of_birth DATE         NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    location      VARCHAR(255) NOT NULL,
    phone_number  VARCHAR(255) NOT NULL,
    account_id    BIGINT       NOT NULL,
    PRIMARY KEY (id),
    KEY (account_id),
    CONSTRAINT FOREIGN KEY (account_id) REFERENCES account (id)
);


