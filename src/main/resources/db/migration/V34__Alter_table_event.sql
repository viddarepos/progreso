ALTER TABLE event
    ADD COLUMN created_by         VARCHAR(255),
    ADD COLUMN created_date       TIMESTAMP,
    ADD COLUMN last_modified_by   VARCHAR(255),
    ADD COLUMN last_modified_date TIMESTAMP;