package konkuk.thip.comment.application.service.policy;

import konkuk.thip.common.post.CommentCountUpdatable;

public interface CommentAccessPolicy {
    void validateCommentAccess(CommentCountUpdatable post, Long userId);
}
