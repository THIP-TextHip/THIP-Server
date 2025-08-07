package konkuk.thip.comment.application.service.policy;

import konkuk.thip.common.post.CountUpdatable;

public interface CommentAccessPolicy {
    void validateCommentAccess(CountUpdatable post, Long userId);
}
