package konkuk.thip.comment.adapter.out.persistence;

/**
 * 댓글 관련 캐시 키 상수
 */
public final class CommentCacheKey {

    private CommentCacheKey() { }

    /**
     * 루트 댓글 캐시 키
     * Format: root_comments:{postId}
     */
    public static final String ROOT_COMMENTS = "root_comments";
}
