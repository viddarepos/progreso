ALTER TABLE `progreso`.`event_request`
ADD COLUMN `assignee_id` BIGINT NULL AFTER `status`;
ALTER TABLE `progreso`.`event_request`
ADD CONSTRAINT `event_request_assignee`
  FOREIGN KEY (`assignee_id`)
  REFERENCES `progreso`.`user` (`id`)
  ON DELETE SET NULL;
