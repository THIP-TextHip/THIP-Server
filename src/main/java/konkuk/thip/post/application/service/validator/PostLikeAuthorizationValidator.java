package konkuk.thip.post.application.service.validator;

import konkuk.thip.common.annotation.HelperService;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.post.application.service.policy.PostLikeAccessPolicy;
import konkuk.thip.post.domain.CountUpdatable;
import konkuk.thip.post.domain.PostType;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@HelperService
@RequiredArgsConstructor
public class PostLikeAuthorizationValidator {

    private final Map<PostType, PostLikeAccessPolicy> likeAccessPolicyMap;

    public void validateUserCanAccessPostLike(PostType type, CountUpdatable post, Long userId) {
        getPolicy(type).validatePostLikeAccess(post, userId);
    }

    public void validateUserCanLike(boolean alreadyLiked) {
        if (alreadyLiked) {
            throw new InvalidStateException(POST_ALREADY_LIKED);
        }
    }

    public void validateUserCanUnLike(boolean alreadyLiked) {
        if (!alreadyLiked) {
            throw new InvalidStateException(POST_NOT_LIKED_CANNOT_CANCEL);
        }
    }

    private PostLikeAccessPolicy getPolicy(PostType type) {
        PostLikeAccessPolicy policy = likeAccessPolicyMap.get(type);
        if (policy == null) {
            throw new InvalidStateException(POST_TYPE_NOT_MATCH);
        }
        return policy;
    }
}
