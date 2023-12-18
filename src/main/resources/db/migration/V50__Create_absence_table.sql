CREATE TABLE absence_request (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 title VARCHAR(255) NOT NULL,
                                 description LONGTEXT,
                                 status VARCHAR(30) NOT NULL,
                                 absence_type VARCHAR(30) NOT NULL,
                                 start_date DATE NOT NULL,
                                 end_date DATE NOT NULL,
                                 requester_id BIGINT,
                                 season_id BIGINT,
                                 FOREIGN KEY (requester_id) REFERENCES user (id),
                                 FOREIGN KEY (season_id) REFERENCES season (id)
);
