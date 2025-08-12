package konkuk.thip.vote.domain;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.post.domain.service.PostCountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static konkuk.thip.common.exception.code.ErrorCode.COMMENT_COUNT_UNDERFLOW;
import static konkuk.thip.common.exception.code.ErrorCode.POST_LIKE_COUNT_UNDERFLOW;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("[단위] Vote 도메인 테스트")
class VoteTest {

    private PostCountService postCountService;

    @BeforeEach
    void setUp() {
        postCountService = new PostCountService();
    }

    private final Long CREATOR_ID = 1L;

    private Vote createWithCommentVote() {
        return Vote.builder()
                .id(100L)
                .content("댓글이 존재하는 투표입니다.")
                .creatorId(CREATOR_ID)
                .page(10)
                .isOverview(false)
                .likeCount(0)
                .commentCount(1)
                .roomId(100L)
                .build();
    }

    private Vote createNotCommentVote() {
        return Vote.builder()
                .id(100L)
                .content("댓글이 존재하지 않는 투표입니다.")
                .creatorId(CREATOR_ID)
                .page(10)
                .isOverview(false)
                .likeCount(0)
                .commentCount(0)
                .roomId(100L)
                .build();
    }


    @Test
    @DisplayName("validatePage: 유효한 페이지 범위일 때, 예외가 발생하지 않는다.")
    void validate_page_valid_range() {
        Vote vote = Vote.withoutId("content", 1L, 10, false, 1L);
        assertDoesNotThrow(() -> vote.validatePage(20));
        assertDoesNotThrow(() -> vote.validatePage(10));  // 경계값
    }

    @Test
    @DisplayName("validatePage: page가 1보다 작을 때, InvalidStateException 발생한다.")
    void validate_page_lower_than_zero() {
        Vote vote = Vote.withoutId("content", 1L, 0, false, 1L);
        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> vote.validatePage(20));
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("현재 기록할 page = 0, 책 전체 page = 20"));
    }

    @Test
    @DisplayName("validatePage: page가 전체 페이지 수를 초과할 때, InvalidStateException 발생한다.")
    void validate_page_bigger_than_total() {
        Vote vote = Vote.withoutId("content", 1L, 25, false, 1L);
        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> vote.validatePage(20));
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("현재 기록할 page = 25, 책 전체 page = 20"));
    }

    @Test
    @DisplayName("validateOverview: isOverview=false 이면, 예외가 발생하지 않는다.")
    void validate_overview_not_overview_no_exception() {
        Vote vote = Vote.withoutId("content", 1L, 5, false, 1L);
        assertDoesNotThrow(() -> vote.validateOverview(20));
    }

    @Test
    @DisplayName("validateOverview: 진행률 80% 이상이고 isOverview=true 이면, 예외가 발생하지 않는다.")
    void validate_overview_ratio_at_least_80_percent() {
        Vote vote = Vote.withoutId("content", 1L, 16, true, 1L);
        assertDoesNotThrow(() -> vote.validateOverview(20));
    }

    @Test
    @DisplayName("validateOverview: 진행률 80% 미만이고 isOverview=true 이면, InvalidStateException 발생한다.")
    void validate_overview_ratio_below_80_percent() {
        Vote vote = Vote.withoutId("content", 1L, 15, true, 1L);  // 15/20 = 0.75
        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> vote.validateOverview(20));
        assertInstanceOf(IllegalStateException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("현재 진행률 = 75.00% (15/20)"));
    }

    @Test
    @DisplayName("increaseCommentCount: 투표의 댓글 수가 정상적으로 1 증가한다.")
    void increaseCommentCount_increments() {
        Vote vote = createWithCommentVote();
        int before = vote.getCommentCount();

        vote.increaseCommentCount();

        assertEquals(before + 1, vote.getCommentCount());
    }

    @Test
    @DisplayName("decreaseCommentCount: 투표의 댓글 수가 정상적으로 1 감소한다.")
    void decreaseCommentCount_decrements() {
        Vote vote = createWithCommentVote();
        int before = vote.getCommentCount();

        vote.decreaseCommentCount();

        assertEquals(before - 1, vote.getCommentCount());
    }

    @Test
    @DisplayName("decreaseCommentCount: 투표의 댓글 수가 0 이하로 내려가면 InvalidStateException이 발생한다.")
    void decreaseCommentCount_belowZero_throws() {
        Vote vote = createNotCommentVote();

        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> vote.decreaseCommentCount());

        assertEquals(COMMENT_COUNT_UNDERFLOW, ex.getErrorCode());
    }

    @Test
    @DisplayName("updateLikeCount: like == true 면 likeCount 가 1씩 증가한다.")
    void updateLikeCount_likeTrue_increments() {
        Vote vote = createWithCommentVote();

        vote.updateLikeCount(postCountService,true);
        assertEquals(1, vote.getLikeCount());

        vote.updateLikeCount(postCountService,true);
        assertEquals(2, vote.getLikeCount());
    }

    @Test
    @DisplayName("updateLikeCount: like == false 면 likeCount 가 1씩 감소한다.")
    void updateLikeCount_likeFalse_decrements() {
        Vote vote = createWithCommentVote();

        // 먼저 likeCount 증가 셋업
        vote.updateLikeCount(postCountService,true);
        vote.updateLikeCount(postCountService,true);
        assertEquals(2, vote.getLikeCount());

        vote.updateLikeCount(postCountService,false);
        assertEquals(1, vote.getLikeCount());

        vote.updateLikeCount(postCountService,false);
        assertEquals(0, vote.getLikeCount());
    }

    @Test
    @DisplayName("updateLikeCount: like == false 면 likeCount 가 0 이하로 내려가면 InvalidStateException이 발생한다.")
    void updateLikeCount_likeFalse_underflow_throws() {
        Vote vote = createWithCommentVote();
        assertEquals(0, vote.getLikeCount());

        InvalidStateException ex = assertThrows(InvalidStateException.class, () -> {
            vote.updateLikeCount(postCountService,false);
        });

        assertEquals(POST_LIKE_COUNT_UNDERFLOW, ex.getErrorCode());
    }

}
