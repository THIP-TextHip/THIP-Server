package konkuk.thip.post.application.port.in.dto;

import konkuk.thip.common.post.PostType;

public record PostIsLikeCommand(

        Long userId,

        Long postId,

        PostType postType,

        boolean isLike
)
{
}
