package konkuk.thip.post.application.service.policy;

import konkuk.thip.common.post.CountUpdatable;

public interface PostLikeAccessPolicy {
    void validatePostLikeAccess(CountUpdatable post, Long userId);
}
