package konkuk.thip.comment.domain;

import konkuk.thip.common.exception.InvalidStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static konkuk.thip.common.entity.StatusType.ACTIVE;
import static konkuk.thip.common.exception.code.ErrorCode.*;
import static konkuk.thip.post.domain.PostType.FEED;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("[단위] Comment 단위 테스트")
class CommentTest {

    private final String CONTENT = "댓글 본문";
    private final Long CREATOR_ID = 1L;
    private final Long POST_ID = 100L;
    private final Long OTHER_USER_ID = 2L;

    private Comment createParentComment(Long postId) {
        return Comment.builder()
                .id(123L) //ID 임의 주입
                .content(CONTENT)
                .targetPostId(postId)
                .creatorId(CREATOR_ID)
                .postType(FEED)
                .parentCommentId(null)
                .reportCount(0)
                .likeCount(0)
                .status(ACTIVE)
                .build();
    }

    @Test
    @DisplayName("createComment: 일반 댓글 생성 시 parentId는 null이면 정상적으로 Comment가 생성된다.")
    void createRootComment_valid() {
        Comment comment = Comment.createComment(
                CONTENT,
                POST_ID,
                CREATOR_ID,
                "feed",
                false,
                null,
                null
        );

        assertNotNull(comment);
        assertNull(comment.getParentCommentId());
        assertEquals(FEED, comment.getPostType());
        assertEquals(POST_ID, comment.getTargetPostId());
        assertEquals(CONTENT, comment.getContent());
    }

    @Test
    @DisplayName("createComment: 답글 생성 시 parentComment 존재 + 게시글 ID 일치하면 정상적으로 Comment가 생성된다.")
    void createReplyComment_valid() {
        Comment parent = createParentComment(POST_ID);

        Comment reply = Comment.createComment(
                "답글입니다.",
                POST_ID,
                CREATOR_ID,
                "feed",
                true,
                parent.getId(),
                parent
        );

        assertNotNull(reply);
        assertEquals(parent.getId(), reply.getParentCommentId());
        assertEquals(FEED, reply.getPostType());
    }

    @Test
    @DisplayName("createComment: 일반 댓글 생성 시 parentId가 존재하면 InvalidStateException 이 발생한다.")
    void createRootComment_withParentId_shouldFail() {
        InvalidStateException ex = assertThrows(InvalidStateException.class, () -> Comment.createComment(
                CONTENT,
                POST_ID,
                CREATOR_ID,
                "feed",
                false,
                99L,
                null
        ));

        assertEquals("일반 댓글에는 parentId가 없어야 합니다.", ex.getCause().getMessage());
    }

    @Test
    @DisplayName("createComment: 답글 생성 시 parentId가 null이면 InvalidStateException 이 발생한다.")
    void createReplyComment_missingParentId_shouldFail() {
        InvalidStateException ex = assertThrows(InvalidStateException.class, () -> Comment.createComment(
                CONTENT,
                POST_ID,
                CREATOR_ID,
                "feed",
                true,
                null,
                null
        ));

        assertEquals("답글 작성 시 parentId는 필수입니다.", ex.getCause().getMessage());
    }

    @Test
    @DisplayName("createComment: 답글 생성 시 parentComment 가 null 이면 InvalidStateException 이 발생한다.")
    void createReplyComment_missingParentComment_shouldFail() {
        InvalidStateException ex = assertThrows(InvalidStateException.class, () -> Comment.createComment(
                CONTENT,
                POST_ID,
                CREATOR_ID,
                "feed",
                true,
                1L,
                null // parentComment 누락
        ));

        assertEquals("parentId에 해당하는 부모 댓글이 존재해야 합니다.", ex.getCause().getMessage());
    }

    @Test
    @DisplayName("createComment: 답글 생성 시 부모 댓글과 게시글 ID가 일치하지 않으면 InvalidStateException 이 발생한다.")
    void createReplyComment_parentPostMismatch_shouldFail() {
        Comment parent = createParentComment(999L); // 다른 postId

        InvalidStateException ex = assertThrows(InvalidStateException.class, () -> Comment.createComment(
                CONTENT,
                POST_ID,
                CREATOR_ID,
                "feed",
                true,
                parent.getId(),
                parent
        ));

        assertEquals("댓글과 부모 댓글의 게시글이 일치하지 않습니다.", ex.getCause().getMessage());
    }

    @Test
    @DisplayName("updateLikeCount: like == true 면 likeCount 가 1씩 증가한다.")
    void updateLikeCount_likeTrue_increments() {
        Comment comment = createParentComment(POST_ID);

        comment.updateLikeCount(true);
        assertEquals(1, comment.getLikeCount());

        comment.updateLikeCount(true);
        assertEquals(2, comment.getLikeCount());
    }

    @Test
    @DisplayName("updateLikeCount: like == false 면 likeCount 가 1씩 감소한다.")
    void updateLikeCount_likeFalse_decrements() {
        Comment comment = createParentComment(POST_ID);
        // 먼저 likeCount 증가 셋업
        comment.updateLikeCount(true);
        comment.updateLikeCount(true);
        assertEquals(2, comment.getLikeCount());

        comment.updateLikeCount(false);
        assertEquals(1, comment.getLikeCount());

        comment.updateLikeCount(false);
        assertEquals(0, comment.getLikeCount());
    }

    @Test
    @DisplayName("updateLikeCount: like == false 면 likeCount 가 0 이하로 내려가면 InvalidStateException이 발생한다.")
    void updateLikeCount_likeFalse_underflow_throws() {
        Comment comment = createParentComment(POST_ID);
        assertEquals(0, comment.getLikeCount());

        InvalidStateException ex = assertThrows(InvalidStateException.class, () -> {
            comment.updateLikeCount(false);
        });

        assertEquals(COMMENT_LIKE_COUNT_UNDERFLOW, ex.getErrorCode());
    }
}
