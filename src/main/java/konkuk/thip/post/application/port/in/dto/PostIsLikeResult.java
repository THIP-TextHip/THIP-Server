package konkuk.thip.post.application.port.in.dto;

public record PostIsLikeResult(
        Long postId,
        boolean isLiked
)
{
    public static PostIsLikeResult of(Long postId, boolean isLiked) {
        return new PostIsLikeResult(postId, isLiked);
    }
}