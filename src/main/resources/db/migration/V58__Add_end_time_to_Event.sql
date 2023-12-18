ALTER TABLE event
ADD COLUMN end_time datetime;

ALTER TABLE event_aud
ADD COLUMN end_time datetime;

UPDATE event
SET end_time = start_time + INTERVAL duration/1000000000 SECOND;

UPDATE event_aud
SET end_time = start_time + INTERVAL duration/1000000000 SECOND;