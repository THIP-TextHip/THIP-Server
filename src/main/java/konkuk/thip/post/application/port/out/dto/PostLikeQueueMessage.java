package konkuk.thip.post.application.port.out.dto;

import konkuk.thip.post.domain.PostType;

public record PostLikeQueueMessage(
    Long userId,
    Long postId,
    String action, // "SAVE" or "DELETE"
    PostType postType
) {
}