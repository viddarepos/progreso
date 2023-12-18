CREATE TABLE `events_attendees`
(
    `event_id` BIGINT NOT NULL ,
    `user_id` BIGINT NOT NULL ,
    KEY (`event_id`),
    KEY (`user_id`),
    CONSTRAINT `fk_event_user` FOREIGN KEY (`event_id`) REFERENCES `event`(`id`),
    CONSTRAINT `fk_user_event` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
)