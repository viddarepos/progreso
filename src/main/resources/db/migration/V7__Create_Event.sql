CREATE TABLE event (
    `id`            BIGINT NOT NULL AUTO_INCREMENT,
    `title`         VARCHAR(255) NOT NULL CHECK (CHAR_LENGTH(title) >= 2 AND CHAR_LENGTH(title) <= 64),
    `description`   VARCHAR(255) CHECK (CHAR_LENGTH(`description`) <= 512),
    `start_time`    DATETIME NOT NULL,
    `duration`      TIME NOT NULL,
    `creator_id`    BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY(`creator_id`),
    CONSTRAINT FOREIGN KEY (`creator_id`) REFERENCES user (id)
);