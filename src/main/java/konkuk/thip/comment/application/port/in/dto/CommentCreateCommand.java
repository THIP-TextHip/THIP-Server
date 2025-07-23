package konkuk.thip.comment.application.port.in.dto;

public record CommentCreateCommand(

        String content,

        Boolean isReplyRequest,

        Long parentId,

        String postType,

        Long postId,

        Long userId
)
{
}
