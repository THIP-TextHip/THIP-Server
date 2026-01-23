-- Step 1: 컬럼 추가 (Nullable)
ALTER TABLE comments ADD COLUMN root_comment_id BIGINT;

-- Step 2: 자식 댓글 데이터 마이그레이션 (Recursive CTE)
-- MySQL 8.0+
WITH RECURSIVE comment_path AS (
    -- Anchor: 부모가 없는 루트 댓글들
    -- 주의: 루트 댓글의 root_comment_id 컬럼은 NULL이지만, 자식들에게는 이 루트의 ID(comment_id)가 root_id가 됨
    SELECT comment_id, comment_id as root_id, 1 as depth
    FROM comments
    WHERE parent_id IS NULL

    UNION ALL

    -- Recursive: 부모를 따라가며 자식 찾기
    SELECT c.comment_id, cp.root_id, cp.depth + 1
    FROM comments c
             INNER JOIN comment_path cp ON c.parent_id = cp.comment_id
    WHERE cp.depth < 1000 -- 무한 루프 방지용 깊이 제한
)
UPDATE comments c
    INNER JOIN comment_path path ON c.comment_id = path.comment_id
    SET c.root_comment_id = path.root_id
-- [중요] 루트 댓글(parent_id IS NULL)은 root_comment_id가 NULL이어야 하므로 업데이트 대상에서 제외
WHERE c.parent_id IS NOT NULL;

-- Step 3: 외래 키 제약 조건 추가
-- MySQL InnoDB에서는 FK 생성 시 해당 컬럼에 인덱스가 없으면 자동으로 생성해줍니다.
ALTER TABLE comments
    ADD CONSTRAINT fk_comments_root_comment_id
        FOREIGN KEY (root_comment_id) REFERENCES comments(comment_id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE;