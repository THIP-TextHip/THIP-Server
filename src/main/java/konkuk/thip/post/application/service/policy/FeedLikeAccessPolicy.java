package konkuk.thip.post.application.service.policy;

import konkuk.thip.common.post.CountUpdatable;
import konkuk.thip.feed.domain.Feed;
import org.springframework.stereotype.Component;

@Component
public class FeedLikeAccessPolicy implements PostLikeAccessPolicy {

    @Override
    public void validatePostLikeAccess(CountUpdatable post, Long userId) {
        Feed feed = (Feed) post;
        feed.validateLike(userId);
    }
}
