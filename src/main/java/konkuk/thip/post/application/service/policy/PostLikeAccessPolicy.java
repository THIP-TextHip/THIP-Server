package konkuk.thip.post.application.service.policy;

import konkuk.thip.post.domain.CountUpdatable;

public interface PostLikeAccessPolicy {
    void validatePostLikeAccess(CountUpdatable post, Long userId);
}
