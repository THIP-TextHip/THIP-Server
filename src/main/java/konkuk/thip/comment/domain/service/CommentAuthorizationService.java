package konkuk.thip.comment.domain.service;

import konkuk.thip.comment.domain.Comment;
import konkuk.thip.comment.domain.policy.CommentAccessPolicy;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.post.CommentCountUpdatable;
import konkuk.thip.common.post.PostType;
import konkuk.thip.common.post.service.PostQueryService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static konkuk.thip.common.exception.code.ErrorCode.POST_TYPE_NOT_MATCH;

@Component
public class CommentAuthorizationService {

    private final PostQueryService postQueryService;
    private final Map<PostType, CommentAccessPolicy> policyMap;

    public CommentAuthorizationService(PostQueryService postQueryService, List<CommentAccessPolicy> policies) {
        this.postQueryService = postQueryService;
        this.policyMap = new HashMap<>();
        for (CommentAccessPolicy policy : policies) {
            for (PostType type : PostType.values()) {
                if (policy.supports(type)) {
                    policyMap.put(type, policy);
                }
            }
        }
    }

    private void validateUserCanAccessPostForComment(PostType type, Long postId, Long userId) {
        CommentCountUpdatable post = postQueryService.findPost(type, postId);
        getPolicy(type).validateCommentAccess(post, userId);
    }

    public void validateUserCanAccessPostForComment(PostType type, CommentCountUpdatable post, Long userId) {
        getPolicy(type).validateCommentAccess(post, userId);
    }

    public void validateUserCanAccessPostForComment(Comment comment, Long userId) {
        validateUserCanAccessPostForComment(comment.getPostType(), comment.getTargetPostId(), userId);
    }


    private CommentAccessPolicy getPolicy(PostType type) {
        CommentAccessPolicy policy = policyMap.get(type);
        if (policy == null) {
            throw new InvalidStateException(POST_TYPE_NOT_MATCH);
        }
        return policy;
    }
}
