CREATE TABLE season
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `name`           VARCHAR(255) NOT NULL CHECK (CHAR_LENGTH(name) >= 2 AND CHAR_LENGTH(name) <= 64),
    `start_date`     DATE         NOT NULL,
    `end_date`       DATE         NOT NULL,
    `duration_value` INT          NOT NULL CHECK (duration_value > 0),
    `duration_type`  VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);