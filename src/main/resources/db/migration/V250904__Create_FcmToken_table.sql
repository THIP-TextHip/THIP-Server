CREATE TABLE fcm_tokens (
    fcm_token_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fcm_token VARCHAR(255) NOT NULL,
    device_id VARCHAR(128) NOT NULL UNIQUE,
    platform VARCHAR(16) NOT NULL,
    last_used_time DATE NOT NULL,
    is_enabled BOOLEAN NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    modified_at DATETIME(6) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_fcm_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
            ON DELETE CASCADE
);