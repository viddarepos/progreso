CREATE TABLE `event_request`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title`       VARCHAR(255) NOT NULL,
    `description` VARCHAR(512),
    `requester_id`   BIGINT       NOT NULL,
    `status` VARCHAR(30) NOT NULL ,
    KEY (`requester_id`),
    CONSTRAINT `event_request_user`
        FOREIGN KEY (`requester_id`)
            REFERENCES `user` (`id`)
);
