package konkuk.thip.comment.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.post.domain.PostType;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

import static konkuk.thip.common.exception.code.ErrorCode.*;
import static konkuk.thip.common.exception.code.ErrorCode.COMMENT_NOT_LIKED_CANNOT_CANCEL;

@Getter
@SuperBuilder
public class Comment extends BaseDomainEntity {

    private Long id;

    private String content;

    @Builder.Default
    private int reportCount = 0;

    @Builder.Default
    private int likeCount = 0;

    /**
     * 루트 댓글에서만 의미있는 값
     */
    @Builder.Default
    private int descendantCount = 0;

    private Long targetPostId;

    private Long creatorId;

    private Long parentCommentId;

    /**
     * PersistenceAdapter 에서 Comment -> CommentJpaEntity 변환 시에 rootCommentId 값 세팅
     * 코드 수정 최소화를 위해 Builder Default 로 null 설정
     */
    @Builder.Default
    private Long rootCommentId = null;

    private PostType postType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static Comment createRootComment(String content, Long postId, Long creatorId, PostType postType) {
        return Comment.builder()
                .id(null)
                .content(content)
                .targetPostId(postId)
                .creatorId(creatorId)
                .parentCommentId(null)
                .postType(postType)
                .reportCount(0)
                .likeCount(0)
                .build();
    }

    public static Comment createChildComment(String content, Long postId, Long creatorId, Comment parentComment, PostType postType) {
        validateParentComment(postId, parentComment);
        return Comment.builder()
                .id(null)
                .content(content)
                .targetPostId(postId)
                .creatorId(creatorId)
                .parentCommentId(parentComment.getId())
                .postType(postType)
                .reportCount(0)
                .likeCount(0)
                .build();
    }

    private static void validateParentComment(Long targetPostId, Comment parentComment) {
        if (parentComment == null) {
            throw new InvalidStateException(
                    INVALID_COMMENT_CREATE, new IllegalArgumentException("parentId에 해당하는 부모 댓글이 존재해야 합니다."));
        }
        if (!targetPostId.equals(parentComment.getTargetPostId())) {
            throw new InvalidStateException(
                    INVALID_COMMENT_CREATE, new IllegalArgumentException("댓글과 부모 댓글의 게시글이 일치하지 않습니다."));
        }
    }

    public void updateLikeCount(Boolean like) {
        if (like) {
            likeCount++;
        } else {
            checkLikeCountNotUnderflow();
            likeCount--;
        }
    }

    private void checkLikeCountNotUnderflow() {
        if (likeCount <= 0) {
            throw new InvalidStateException(COMMENT_LIKE_COUNT_UNDERFLOW);
        }
    }

    // 좋아요 생성 가능 여부 검증 (이미 좋아요한 상태면 예외)
    public void validateCanLike(boolean alreadyLiked) {
        if (alreadyLiked) {
            throw new InvalidStateException(COMMENT_ALREADY_LIKED);
        }
    }

    // 좋아요 취소 가능 여부 검증 (좋아요 안 한 상태면 예외)
    public void validateCanUnlike(boolean alreadyLiked) {
        if (!alreadyLiked) {
            throw new InvalidStateException(COMMENT_NOT_LIKED_CANNOT_CANCEL);
        }
    }

    private boolean validateCreator(Long userId) {
        return this.creatorId.equals(userId);
    }

    public void validateDeletable(Long userId) {
        if (!validateCreator(userId)) {
            throw new InvalidStateException(COMMENT_DELETE_FORBIDDEN);
        }
    }

}
