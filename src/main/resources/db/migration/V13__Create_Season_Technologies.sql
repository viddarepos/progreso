CREATE TABLE season_technologies
(
    technology_id BIGINT NOT NULL,
    season_id     BIGINT NOT NULL,
    KEY (technology_id),
    KEY (season_id),
    CONSTRAINT FOREIGN KEY (technology_id) REFERENCES technology (id),
    CONSTRAINT FOREIGN KEY (season_id) REFERENCES season (id)
);