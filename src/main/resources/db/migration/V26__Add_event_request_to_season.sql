ALTER TABLE event_request
ADD season_id BIGINT NOT NULL,
ADD CONSTRAINT FOREIGN KEY(season_id) REFERENCES season(id);