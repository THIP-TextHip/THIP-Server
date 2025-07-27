package konkuk.thip.comment.application.service.policy;

import konkuk.thip.common.post.CommentCountUpdatable;
import konkuk.thip.common.post.PostType;
import konkuk.thip.feed.domain.Feed;
import org.springframework.stereotype.Component;

@Component
public class FeedCommentAccessPolicy implements CommentAccessPolicy {

    @Override
    public boolean supports(PostType type) {
        return type == PostType.FEED;
    }

    @Override
    public void validateCommentAccess(CommentCountUpdatable post, Long userId) {
        Feed feed = (Feed) post;
        feed.validateCreateComment(userId);

    }
}
