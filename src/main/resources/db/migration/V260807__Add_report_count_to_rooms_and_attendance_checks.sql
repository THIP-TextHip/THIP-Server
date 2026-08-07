ALTER TABLE rooms
    ADD COLUMN report_count INT NOT NULL DEFAULT 0;

ALTER TABLE attendance_checks
    ADD COLUMN report_count INT NOT NULL DEFAULT 0;

UPDATE posts
SET report_count = 0
WHERE report_count IS NULL;

ALTER TABLE posts
    MODIFY COLUMN report_count INT NOT NULL DEFAULT 0;
