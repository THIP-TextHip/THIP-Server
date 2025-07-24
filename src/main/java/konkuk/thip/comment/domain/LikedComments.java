package konkuk.thip.comment.domain;

import konkuk.thip.common.exception.InvalidStateException;
import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Getter
public class LikedComments {

    private final Set<Comment> comments;

    public LikedComments(List<Comment> comments) {
        Set<Comment> commentSet = new HashSet<>(comments);
        if (commentSet.size() != comments.size()) {
            throw new InvalidStateException(DUPLICATED_COMMENTS_IN_COLLECTION);
        }
        this.comments = Collections.unmodifiableSet(commentSet);
    }

    // 중복 좋아요 검증
    public void validateNotAlreadyLiked(Comment comment) {
        if (comments.contains(comment)) {
            throw new InvalidStateException(COMMENT_ALREADY_LIKED);
        }
    }

    // 좋아요 취소 가능 여부 검증
    public void validateCanUnlike(Comment comment) {
        if (!comments.contains(comment)) {
            throw new InvalidStateException(COMMENT_NOT_LIKED_CANNOT_CANCEL);
        }
    }

}


