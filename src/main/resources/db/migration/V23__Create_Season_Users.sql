CREATE TABLE season_users
(
    season_id BIGINT NOT NULL ,
    user_id BIGINT NOT NULL,
    KEY (season_id),
    KEY (user_id),
    CONSTRAINT FOREIGN KEY (season_id) REFERENCES season(id),
    CONSTRAINT FOREIGN KEY (user_id) REFERENCES user(id)
)