ALTER TABLE followings
    ADD CONSTRAINT uq_followings_user_target
        UNIQUE (user_id, following_user_id);