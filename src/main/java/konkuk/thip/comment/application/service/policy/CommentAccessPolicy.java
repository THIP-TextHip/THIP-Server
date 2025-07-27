package konkuk.thip.comment.application.service.policy;

import konkuk.thip.common.post.CommentCountUpdatable;
import konkuk.thip.common.post.PostType;

public interface CommentAccessPolicy {
    boolean supports(PostType type);
    void validateCommentAccess(CommentCountUpdatable post, Long userId);
}
