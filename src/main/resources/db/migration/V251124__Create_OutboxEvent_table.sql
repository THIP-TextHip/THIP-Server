-- OutboxEvent 테이블 생성
CREATE TABLE outbox_events (
                               id              BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- 도메인 정보
                               aggregate_type  VARCHAR(100)      NOT NULL,
                               aggregate_id    BIGINT            NOT NULL,

    -- 이벤트 타입 (USER_FOLLOWED / USER_UNFOLLOWED)
                               event_type      VARCHAR(50)       NOT NULL,

    -- 이벤트 페이로드(JSON)
                               payload         LONGTEXT          NOT NULL,

    -- BaseJpaEntity 공통 컬럼
                               created_at      DATETIME(6)       NOT NULL,
                               modified_at     DATETIME(6)       NOT NULL,
                               status          VARCHAR(20)       NOT NULL,
    -- ↑ BaseJpaEntity.StatusType (ACTIVE 등) 용도로 매핑됨

    -- Outbox 전용 상태 (PENDING / PROCESSED / FAILED)
                               outbox_status   VARCHAR(20)       NOT NULL,

                               processed_at    DATETIME(6)       NULL
);

-- Polling 시 성능을 위한 인덱스 (PENDING 이벤트 우선 조회용)
CREATE INDEX idx_outbox_events_outbox_status_id
    ON outbox_events (outbox_status, id);