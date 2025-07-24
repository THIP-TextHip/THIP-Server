package konkuk.thip.comment.application.port.out;

import konkuk.thip.comment.domain.LikedComments;

public interface CommentLikeQueryPort {
    LikedComments findLikedCommentsByUserId(Long userId);
}
