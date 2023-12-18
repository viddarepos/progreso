ALTER TABLE season_users
DROP KEY user_id,
ADD CONSTRAINT fk_user_id
FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE ON UPDATE CASCADE;