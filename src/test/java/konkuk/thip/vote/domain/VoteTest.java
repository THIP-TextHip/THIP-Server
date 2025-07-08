package konkuk.thip.vote.domain;

import konkuk.thip.common.exception.InvalidStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("[단위] Vote 도메인 테스트")
class VoteTest {

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
}
