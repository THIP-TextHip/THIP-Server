package konkuk.thip.room.adapter.in.web.response;

import konkuk.thip.post.application.port.in.dto.PostIsLikeResult;

public record RoomPostIsLikeResponse(
        Long postId,
        boolean isLiked
) {
        public static RoomPostIsLikeResponse of(PostIsLikeResult postIsLikeResult) {
                return new RoomPostIsLikeResponse(postIsLikeResult.postId(), postIsLikeResult.isLiked());
        }
}