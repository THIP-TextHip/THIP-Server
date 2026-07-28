/* descendant_count 컬럼 추가 및 초기화 */

-- 1. 컬럼 추가
ALTER TABLE comments ADD COLUMN descendant_count INT NOT NULL DEFAULT 0;

-- 2. 루트 댓글별 자식 수 집계 및 업데이트 (MySQL 호환 JOIN 문법)
UPDATE comments parent
    JOIN (
        -- 루트 댓글 별 자손 댓글 개수 count
        SELECT root_comment_id, COUNT(*) as child_cnt
        FROM comments
        WHERE root_comment_id IS NOT NULL   -- 자식 댓글들만
            AND status = 'ACTIVE'           -- JPA 엔티티 로직 반영
        GROUP BY root_comment_id            -- 루트 댓글로 그룹핑
    ) stats ON parent.comment_id = stats.root_comment_id
SET parent.descendant_count = stats.child_cnt
WHERE parent.parent_id IS NULL;     -- 루트 댓글만 업데이트