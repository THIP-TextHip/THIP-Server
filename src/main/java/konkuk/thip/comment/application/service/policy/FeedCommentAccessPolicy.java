package konkuk.thip.comment.application.service.policy;

import konkuk.thip.common.post.CountUpdatable;
import konkuk.thip.feed.domain.Feed;
import org.springframework.stereotype.Component;

@Component
public class FeedCommentAccessPolicy implements CommentAccessPolicy {

    @Override
    public void validateCommentAccess(CountUpdatable post, Long userId) {
        Feed feed = (Feed) post;
        feed.validateCreateComment(userId);

    }
}
