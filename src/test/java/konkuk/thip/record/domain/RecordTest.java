package konkuk.thip.record.domain;

import konkuk.thip.common.exception.InvalidStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("[단위] Record 도메인 테스트")
class RecordTest {

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
}