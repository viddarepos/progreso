CREATE TABLE users_integrations
(
    user_id BIGINT NOT NULL ,
    integration_type VARCHAR(255) NOT NULL,
    PRIMARY KEY (`user_id`, `integration_type`),
    CONSTRAINT FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE ON UPDATE CASCADE
)