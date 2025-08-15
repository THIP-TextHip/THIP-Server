package konkuk.thip.feed.application.service.policy;

import konkuk.thip.post.application.service.policy.PostLikeAccessPolicy;
import konkuk.thip.post.domain.CountUpdatable;
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
