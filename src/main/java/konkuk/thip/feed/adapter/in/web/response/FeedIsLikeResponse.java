package konkuk.thip.feed.adapter.in.web.response;

import konkuk.thip.post.application.port.in.dto.PostIsLikeResult;

public record FeedIsLikeResponse(
        Long feedId,
        boolean isLiked
) {
        public static FeedIsLikeResponse of(PostIsLikeResult postIsLikeResult) {
                return new FeedIsLikeResponse(postIsLikeResult.postId(), postIsLikeResult.isLiked());
        }
}
