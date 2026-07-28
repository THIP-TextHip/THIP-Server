-- pageCount를 알라딘 API로 조회했으나 영구적으로 찾을 수 없는 책을 표시하는 플래그
-- true : 알라딘 미등록 → 스케줄러 조회 대상에서 영구 제외
-- false(기본값) : 아직 포기하지 않음 → 스케줄러가 pageCount 채우기 시도
ALTER TABLE books
    ADD COLUMN page_count_unfindable TINYINT(1) NOT NULL DEFAULT 0;

-- 스케줄러 쿼리 최적화 인덱스
-- 대상 조건: page_count IS NULL AND page_count_unfindable = false
-- page_count를 선두 컬럼으로 설정 → 스테디 스테이트에서 IS NULL 선택도가 높아 효과적
CREATE INDEX idx_books_scheduler ON books (page_count, page_count_unfindable);