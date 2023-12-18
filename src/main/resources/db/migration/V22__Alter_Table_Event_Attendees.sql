ALTER TABLE `events_attendees`
ADD COLUMN `required` BOOLEAN NOT NULL DEFAULT 0 AFTER `user_id`;

ALTER TABLE `events_attendees`
ADD COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT,
ADD PRIMARY KEY (`id`);

ALTER TABLE `events_attendees`
DROP CONSTRAINT `fk_event_user`;

ALTER TABLE `events_attendees`
DROP CONSTRAINT `fk_user_event`;

ALTER TABLE `events_attendees`
ADD CONSTRAINT `fk_event_user` FOREIGN KEY (`event_id`) REFERENCES `event`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `events_attendees`
ADD CONSTRAINT `fk_user_event` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;