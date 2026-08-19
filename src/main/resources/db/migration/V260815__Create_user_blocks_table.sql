-- 사용자 차단 테이블. 차단 해제는 row 삭제(hard delete)로 처리한다.
CREATE TABLE user_blocks (
    block_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,          -- 차단한 사용자
    blocked_user_id BIGINT NOT NULL,          -- 차단당한 사용자
    created_at      DATETIME(6) NOT NULL,
    modified_at     DATETIME(6) NOT NULL,
    status          VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uq_user_blocks_user_target UNIQUE (user_id, blocked_user_id),
    CONSTRAINT fk_user_blocks_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
            ON DELETE CASCADE,
    CONSTRAINT fk_user_blocks_blocked_user
        FOREIGN KEY (blocked_user_id) REFERENCES users(user_id)
            ON DELETE CASCADE
);

-- "나를 차단한 사용자" 역방향 조회용
CREATE INDEX idx_user_blocks_reverse ON user_blocks (blocked_user_id, user_id);
