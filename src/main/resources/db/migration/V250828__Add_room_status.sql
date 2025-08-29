-- 1) 컬럼 추가: 기본값 RECRUITING, NOT NULL
ALTER TABLE rooms
    ADD COLUMN room_status VARCHAR(32) NOT NULL DEFAULT 'RECRUITING'
        COMMENT 'Room 상태: RECRUITING/IN_PROGRESS/EXPIRED';

-- 2) start_date가 현재 날짜보다 "이후"이면 IN_PROGRESS로 업데이트
UPDATE rooms
SET room_status = 'IN_PROGRESS'
WHERE start_date >= CURDATE();

-- 3) end_date가 현재 날짜보다 "이전"이면 EXPIRED로 업데이트
UPDATE rooms
SET room_status = 'EXPIRED'
WHERE end_date < CURDATE();