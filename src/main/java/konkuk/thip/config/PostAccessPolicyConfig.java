package konkuk.thip.config;

import konkuk.thip.comment.application.service.policy.CommentAccessPolicy;
import konkuk.thip.comment.application.service.policy.FeedCommentAccessPolicy;
import konkuk.thip.comment.application.service.policy.RoomPostCommentAccessPolicy;
import konkuk.thip.common.post.PostType;
import konkuk.thip.post.application.service.policy.FeedLikeAccessPolicy;
import konkuk.thip.post.application.service.policy.PostLikeAccessPolicy;
import konkuk.thip.post.application.service.policy.RoomPostLikeAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PostAccessPolicyConfig {

    private final FeedCommentAccessPolicy feedCommentPolicy;
    private final RoomPostCommentAccessPolicy roomCommentPolicy;

    private final FeedLikeAccessPolicy feedLikePolicy;
    private final RoomPostLikeAccessPolicy roomLikePolicy;

    @Bean
    public Map<PostType, CommentAccessPolicy> commentAccessPolicyMap() {
        return Map.of(
                PostType.FEED, feedCommentPolicy,
                PostType.RECORD, roomCommentPolicy,
                PostType.VOTE, roomCommentPolicy
        );
    }

    @Bean
    public Map<PostType, PostLikeAccessPolicy> roomPostAccessPolicyMap() {
        return Map.of(
                PostType.FEED, feedLikePolicy,
                PostType.RECORD, roomLikePolicy,
                PostType.VOTE, roomLikePolicy
        );
    }
}