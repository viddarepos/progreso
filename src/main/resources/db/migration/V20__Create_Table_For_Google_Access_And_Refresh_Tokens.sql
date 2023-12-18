CREATE TABLE google_authorization_credentials(
id BIGINT NOT NULL AUTO_INCREMENT,
access_token varchar(1024) ,
refresh_token varchar(1024),
expiration_date_time TIMESTAMP,
PRIMARY KEY (`id`)
);
