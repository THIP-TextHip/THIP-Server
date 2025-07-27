package konkuk.thip.comment.application.service.validator;

import konkuk.thip.comment.application.service.policy.CommentAccessPolicy;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.post.CommentCountUpdatable;
import konkuk.thip.common.post.PostType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

import static konkuk.thip.common.exception.code.ErrorCode.POST_TYPE_NOT_MATCH;

@Component
@RequiredArgsConstructor
public class CommentAuthorizationValidator {

    private final Map<PostType, CommentAccessPolicy> policyMap;

    public void validateUserCanAccessPostForComment(PostType type, CommentCountUpdatable post, Long userId) {
        getPolicy(type).validateCommentAccess(post, userId);
    }

    private CommentAccessPolicy getPolicy(PostType type) {
        CommentAccessPolicy policy = policyMap.get(type);
        if (policy == null) {
            throw new InvalidStateException(POST_TYPE_NOT_MATCH);
        }
        return policy;
    }
}
