ALTER TABLE users
    ADD COLUMN record_review_count INT NOT NULL DEFAULT 0 AFTER follower_count;

UPDATE users
SET record_review_count = 0
WHERE record_review_count IS NULL;