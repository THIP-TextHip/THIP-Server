ALTER TABLE users
    ADD FULLTEXT INDEX idx_users_nickname (nickname);