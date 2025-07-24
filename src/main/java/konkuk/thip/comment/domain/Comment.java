package konkuk.thip.comment.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.post.PostType;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

import static konkuk.thip.common.exception.code.ErrorCode.COMMENT_LIKE_COUNT_UNDERFLOW;
import static konkuk.thip.common.exception.code.ErrorCode.INVALID_COMMENT_CREATE;

@Getter
@SuperBuilder
public class Comment extends BaseDomainEntity {

    private Long id;

    private String content;

    @Builder.Default
    private int reportCount = 0;

    @Builder.Default
    private int likeCount = 0;

    private Long targetPostId;

    private Long creatorId;

    private Long parentCommentId;

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


    public static Comment createComment(String content, Long postId, Long creatorId, String type,
                                 boolean isReplyRequest, Long parentId, Comment parent) {

        // 댓글/답글 생성 검증
        validateCommentCreate(isReplyRequest,parentId);
        PostType postType = PostType.from(type);

        if (isReplyRequest) {
            // 답글 생성 검증
            validateReplyCommentCreate(postId, parent);
            return withoutIdReplyComment(content, postId, creatorId, parent, postType);
        }
        return withoutIdRootComment(content, postId, creatorId, postType);
    }


    private static Comment withoutIdRootComment(String content, Long targetPostId, Long creatorId, PostType postType) {
        return Comment.builder()
                .id(null)
                .content(content)
                .targetPostId(targetPostId)
                .creatorId(creatorId)
                .parentCommentId(null)
                .postType(postType)
                .reportCount(0)
                .likeCount(0)
                .build();
    }

    private static Comment withoutIdReplyComment(String content, Long targetPostId, Long creatorId, Comment parentComment, PostType postType) {
        return Comment.builder()
                .id(null)
                .content(content)
                .targetPostId(targetPostId)
                .creatorId(creatorId)
                .parentCommentId(parentComment.getId())
                .postType(postType)
                .reportCount(0)
                .likeCount(0)
                .build();
    }

    private static void validateReplyCommentCreate(Long targetPostId, Comment parentComment) {
        if (parentComment == null) {
            throw new InvalidStateException(
                    INVALID_COMMENT_CREATE,new IllegalArgumentException("parentId에 해당하는 부모 댓글이 존재해야 합니다."));
        }
        if (!targetPostId.equals(parentComment.getTargetPostId())) {
            throw new InvalidStateException(
                    INVALID_COMMENT_CREATE,new IllegalArgumentException("댓글과 부모 댓글의 게시글이 일치하지 않습니다."));
        }
    }

    public static void validateCommentCreate(boolean isReplyRequest, Long parentId) {
        if (isReplyRequest && parentId == null) {
            throw new InvalidStateException(
                    INVALID_COMMENT_CREATE, new IllegalArgumentException("답글 작성 시 parentId는 필수입니다."));

        }
        if (!isReplyRequest && parentId != null) {
            throw new InvalidStateException(
                    INVALID_COMMENT_CREATE, new IllegalArgumentException("일반 댓글에는 parentId가 없어야 합니다."));
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
}
