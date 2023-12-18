CREATE TABLE technology_users
(
    technology_id BIGINT NOT NULL ,
    user_id BIGINT NOT NULL,
    KEY (technology_id),
    KEY (user_id),
    CONSTRAINT FOREIGN KEY (technology_id) REFERENCES technology(id),
    CONSTRAINT FOREIGN KEY (user_id) REFERENCES user(id)
)