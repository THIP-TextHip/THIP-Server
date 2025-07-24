package konkuk.thip.comment.application.port.in.dto;

public record CommentIsLikeCommand(

        Long userId,

        Long commentId,

        Boolean isLike
)
{
}
