package konkuk.thip.comment.application.port.in.dto;


import konkuk.thip.post.domain.PostType;

public record CommentShowAllQuery(
        Long postId,
        Long userId,
        PostType postType,
        String cursorStr
) {
    public static CommentShowAllQuery of(Long postId, Long userId, String postTypeStr, String cursorStr) {
        return new CommentShowAllQuery(postId, userId, PostType.from(postTypeStr), cursorStr);     // 내부에서 PostType string 값 유효성 검증
    }
}
