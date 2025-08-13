package konkuk.thip.record.domain;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.post.domain.service.PostCountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static konkuk.thip.common.exception.code.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("[단위] Record 도메인 테스트")
class RecordTest {

    private PostCountService postCountService;

    @BeforeEach
    void setUp() {
        postCountService = new PostCountService();
    }

    private final Long CREATOR_ID = 1L;
    private final Long OTHER_USER_ID = 2L;

    private final Long ROOM_ID = 1L;
    private final Long OTHER_ROOM_ID = 2L;

    private Record createWithCommentRecord() {
        return Record.builder()
                .id(100L)
                .content("댓글이 존재하는 기록입니다.")
                .creatorId(CREATOR_ID)
                .page(10)
                .isOverview(false)
                .likeCount(0)
                .commentCount(1)
                .roomId(ROOM_ID)
                .build();
    }

    private Record createNotCommentRecord() {
        return Record.builder()
                .id(100L)
                .content("댓글이 존재하지 않는 기록입니다.")
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
        Record record = Record.withoutId("content", 1L, 10, false, 1L);
        assertDoesNotThrow(() -> record.validatePage(20));
        assertDoesNotThrow(() -> record.validatePage(10));
    }

    @Test
    @DisplayName("validatePage: page가 1보다 작을 때, InvalidStateException 발생한다.")
    void validate_page_lower_than_zero() {
        Record record = Record.withoutId("content", 1L, 0, false, 1L);
        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> record.validatePage(20));
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("현재 기록할 page = 0, 책 전체 page = 20"));
    }

    @Test
    @DisplayName("validatePage: page가 전체 페이지 수를 초과할 때, InvalidStateException 발생한다.")
    void validate_page_bigger_than_total() {
        Record record = Record.withoutId("content", 1L, 25, false, 1L);
        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> record.validatePage(20));
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("현재 기록할 page = 25, 책 전체 page = 20"));
    }

    @Test
    @DisplayName("validateOverview: isOverview=false 이면, 예외가 발생하지 않는다.")
    void validate_overview_not_overview_no_exception() {
        Record record = Record.withoutId("content", 1L, 5, false, 1L);
        assertDoesNotThrow(() -> record.validateOverview(20));
    }

    @Test
    @DisplayName("validateOverview: isOverview=true 이고 page가 전체 페이지 수와 같으면, 예외가 발생하지 않는다.")
    void validate_overview_page_is_book_page_count() {
        Record record = Record.withoutId("content", 1L, 100, true, 1L);
        assertDoesNotThrow(() -> record.validateOverview(100));
    }

    @Test
    @DisplayName("validateOverview: isOverview=true 이고 page가 전체 페이지 수와 다르면, InvalidStateException 발생한다.")
    void validate_overview_page_is_not_book_page_count() {
        Record record = Record.withoutId("content", 1L, 15, true, 1L);  // 15/20 = 0.75
        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> record.validateOverview(20));
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("현재 페이지 = 15"));
    }

    @Test
    @DisplayName("increaseCommentCount: 기록의 댓글 수가 정상적으로 1 증가한다.")
    void increaseCommentCount_increments() {
        Record record = createWithCommentRecord();
        int before = record.getCommentCount();

        record.increaseCommentCount();

        assertEquals(before + 1, record.getCommentCount());
    }

    @Test
    @DisplayName("decreaseCommentCount: 기록의 댓글 수가 정상적으로 1 감소한다.")
    void decreaseCommentCount_decrements() {
        Record record = createWithCommentRecord();
        int before = record.getCommentCount();

        record.decreaseCommentCount();

        assertEquals(before - 1, record.getCommentCount());
    }

    @Test
    @DisplayName("decreaseCommentCount: 기록의 댓글 수가 0 이하로 내려가면 InvalidStateException이 발생한다.")
    void decreaseCommentCount_belowZero_throws() {
        Record record = createNotCommentRecord();

        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> record.decreaseCommentCount());

        assertEquals(COMMENT_COUNT_UNDERFLOW, ex.getErrorCode());
    }

    @Test
    @DisplayName("updateLikeCount: like == true 면 likeCount 가 1씩 증가한다.")
    void updateLikeCount_likeTrue_increments() {
        Record record = createWithCommentRecord();

        record.updateLikeCount(postCountService,true);
        assertEquals(1, record.getLikeCount());

        record.updateLikeCount(postCountService,true);
        assertEquals(2, record.getLikeCount());
    }

    @Test
    @DisplayName("updateLikeCount: like == false 면 likeCount 가 1씩 감소한다.")
    void updateLikeCount_likeFalse_decrements() {
        Record record = createWithCommentRecord();

        // 먼저 likeCount 증가 셋업
        record.updateLikeCount(postCountService,true);
        record.updateLikeCount(postCountService,true);
        assertEquals(2, record.getLikeCount());

        record.updateLikeCount(postCountService,false);
        assertEquals(1, record.getLikeCount());

        record.updateLikeCount(postCountService,false);
        assertEquals(0, record.getLikeCount());
    }

    @Test
    @DisplayName("updateLikeCount: like == false 면 likeCount 가 0 이하로 내려가면 InvalidStateException이 발생한다.")
    void updateLikeCount_likeFalse_underflow_throws() {
        Record record = createWithCommentRecord();
        assertEquals(0, record.getLikeCount());

        InvalidStateException ex = assertThrows(InvalidStateException.class, () -> {
            record.updateLikeCount(postCountService,false);
        });

        assertEquals(POST_LIKE_COUNT_UNDERFLOW, ex.getErrorCode());
    }

    @Test
    @DisplayName("validateDeletable: 작성자가 아닌 경우 기록을 삭제하려고 하면 InvalidStateException이 발생한다.")
    void validateDeletable_byNonCreator_throws(){
        Record record = createWithCommentRecord();
        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> record.validateDeletable(OTHER_USER_ID,ROOM_ID));

        assertEquals(RECORD_ACCESS_FORBIDDEN, ex.getErrorCode());
    }

    @Test
    @DisplayName("validateDeletable: 전달된 roomId가 기록의 roomId가 일치 하지않은 경우 기록을 삭제하려고 하면 InvalidStateException이 발생한다.")
    void validateDeletable_byOtherRoomId_throws(){
        Record record = createWithCommentRecord();
        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> record.validateDeletable(CREATOR_ID,OTHER_ROOM_ID));

        assertEquals(RECORD_ACCESS_FORBIDDEN, ex.getErrorCode());
    }

    @Test
    @DisplayName("validateDeletable: 기록의 작성자면서, 전달된 roomId가 기록의 roomId와 일치할 경우 기록을 삭제 할 수 있다.")
    void validateDeletable_byCreator_byRoomId_Success(){
        Record record = createWithCommentRecord();
        assertDoesNotThrow(() -> record.validateDeletable(CREATOR_ID,ROOM_ID));
    }

    @Test
    @DisplayName("validatePin: 작성자가 아닌 경우 기록을 핀하려고 하면 InvalidStateException이 발생한다.")
    void validatePin_byNonCreator_throws() {
        Record record = createWithCommentRecord();

        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> record.validatePin(OTHER_USER_ID, ROOM_ID));

        assertEquals(RECORD_ACCESS_FORBIDDEN, ex.getErrorCode());
    }

    @Test
    @DisplayName("validatePin: 전달된 roomId가 기록의 roomId와 일치하지 않는 경우 기록을 핀하려고 하면 InvalidStateException이 발생한다.")
    void validatePin_byOtherRoomId_throws() {
        Record record = createWithCommentRecord();

        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> record.validatePin(CREATOR_ID, OTHER_ROOM_ID));

        assertEquals(RECORD_ACCESS_FORBIDDEN, ex.getErrorCode());
    }

    @Test
    @DisplayName("validatePin: 작성자이면서 roomId가 기록의 roomId와 일치하면 핀 가능하다.")
    void validatePin_byCreator_byRoomId_success() {
        Record record = createWithCommentRecord();

        assertDoesNotThrow(() -> record.validatePin(CREATOR_ID, ROOM_ID));
    }




}