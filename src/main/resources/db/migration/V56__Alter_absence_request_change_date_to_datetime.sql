ALTER TABLE absence_request
    CHANGE COLUMN start_date start_time DATETIME NOT NULL,
    CHANGE COLUMN end_date end_time DATETIME NOT NULL;