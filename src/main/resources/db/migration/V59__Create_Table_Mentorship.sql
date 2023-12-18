CREATE TABLE mentorship (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            intern_id BIGINT NOT NULL,
                            mentor_id BIGINT NOT NULL,
                            season_id BIGINT NOT NULL,
                            start_date DATE NOT NULL,
                            end_date DATE NOT NULL,
                            PRIMARY KEY (id),
                            FOREIGN KEY (intern_id) REFERENCES user (id),
                            FOREIGN KEY (mentor_id) REFERENCES user (id),
                            FOREIGN KEY (season_id) REFERENCES season (id)
);