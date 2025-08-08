package konkuk.thip.comment.application.port.out;

import java.util.Set;

public interface CommentLikeQueryPort {
    boolean isLikedCommentByUser(Long userId, Long commentId);

    Set<Long> findCommentIdsLikedByUser(Set<Long> commentIds, Long userId);
}
