package konkuk.thip.config;

import konkuk.thip.comment.application.service.policy.CommentAccessPolicy;
import konkuk.thip.comment.application.service.policy.FeedCommentAccessPolicy;
import konkuk.thip.comment.application.service.policy.RoomPostCommentAccessPolicy;
import konkuk.thip.common.post.PostType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class CommentAccessPolicyConfig {

    private final FeedCommentAccessPolicy feedCommentPolicy;
    private final RoomPostCommentAccessPolicy roomCommentPolicy;

    @Bean
    public Map<PostType, CommentAccessPolicy> commentAccessPolicyMap() {
        return Map.of(
                PostType.FEED, feedCommentPolicy,
                PostType.RECORD, roomCommentPolicy,
                PostType.VOTE, roomCommentPolicy
        );
    }
}