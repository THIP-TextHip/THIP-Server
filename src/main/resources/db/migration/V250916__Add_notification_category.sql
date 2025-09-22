-- 1단계: NULL 허용으로 notification_category 컬럼 추가
ALTER TABLE notifications
    ADD COLUMN notification_category VARCHAR(16)
            COMMENT '알림 카테고리: FEED or ROOM';

-- 2단계: 기존 데이터 update
UPDATE notifications
SET notification_category = 'FEED'
WHERE notification_category IS NULL;

-- 3단계: NOT NULL 제약 추가
ALTER TABLE notifications
    MODIFY COLUMN notification_category VARCHAR(16) NOT NULL
            COMMENT '알림 카테고리: FEED/ROOM';