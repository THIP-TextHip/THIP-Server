-- 팔로잉 테이블에 사용자와 타겟 사용자 간의 유니크 제약 조건 추가
ALTER TABLE followings
    ADD CONSTRAINT uq_followings_user_target
        UNIQUE (user_id, following_user_id);