ALTER TABLE event
MODIFY COLUMN duration NUMERIC(19,0);

ALTER TABLE event_aud
MODIFY COLUMN duration NUMERIC(19,0);