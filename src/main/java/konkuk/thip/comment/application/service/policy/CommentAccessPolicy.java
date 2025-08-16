package konkuk.thip.comment.application.service.policy;

import konkuk.thip.post.domain.CountUpdatable;

public interface CommentAccessPolicy {
    void validateCommentAccess(CountUpdatable post, Long userId);
}
