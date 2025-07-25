package konkuk.thip.comment.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.InvalidStateException;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import static konkuk.thip.common.exception.code.ErrorCode.COMMENT_ALREADY_LIKED;
import static konkuk.thip.common.exception.code.ErrorCode.COMMENT_NOT_LIKED_CANNOT_CANCEL;

@Getter
@SuperBuilder
public class CommentLike extends BaseDomainEntity {

    private Long id;

    private Long userId;

    private Long targetCommentId;

    // 좋아요 생성 가능 여부 검증 (이미 좋아요한 상태면 예외)
    public static void validateCanLike(boolean alreadyLiked) {
        if (alreadyLiked) {
            throw new InvalidStateException(COMMENT_ALREADY_LIKED);
        }
    }

    // 좋아요 취소 가능 여부 검증 (좋아요 안 한 상태면 예외)
    public static void validateCanUnlike(boolean alreadyLiked) {
        if (!alreadyLiked) {
            throw new InvalidStateException(COMMENT_NOT_LIKED_CANNOT_CANCEL);
        }
    }

}
