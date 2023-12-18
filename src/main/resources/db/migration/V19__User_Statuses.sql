ALTER TABLE account
ADD status VARCHAR(255) NOT NULL;

UPDATE account
SET status='ARCHIVED'
WHERE archived=true;

UPDATE account
SET status='ACTIVE'
WHERE archived=false;

ALTER TABLE account
DROP COLUMN archived;
