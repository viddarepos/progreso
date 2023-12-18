ALTER TABLE season
ADD owner_id BIGINT,
ADD CONSTRAINT FOREIGN KEY(owner_id) REFERENCES user(id);

UPDATE season
SET owner_id=1
WHERE owner_id is NULL;