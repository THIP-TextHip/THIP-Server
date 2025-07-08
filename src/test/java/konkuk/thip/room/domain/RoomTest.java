package konkuk.thip.room.domain;

import konkuk.thip.common.exception.InvalidStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Room 단위 테스트")
class RoomTest {

    private final LocalDate START = LocalDate.of(2025, 7, 1);
    private final LocalDate END   = LocalDate.of(2025, 8, 1);

    @Test
    @DisplayName("withoutId: 공개 방이면서 password가 not null 이면, InvalidStateException 발생한다.")
    void withoutId_public_password_not_null() {
        InvalidStateException ex = assertThrows(InvalidStateException.class, () ->
                Room.withoutId(
                        "제목", "설명", true, "1234",
                        START, END, 5, 123L, 456L
                )
        );
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("방 공개/비공개 여부와 비밀번호 설정이 일치하지 않습니다."));
        assertTrue(ex.getCause().getMessage().contains("공개 여부 = true, 비밀번호 존재 여부 = true"));
    }

    @Test
    @DisplayName("withoutId: 비공개 방이면서 password가 null 이면, InvalidStateException 발생한다.")
    void withoutId_private_password_null() {
        InvalidStateException ex = assertThrows(InvalidStateException.class, () ->
                Room.withoutId(
                        "제목", "설명", false, null,
                        START, END, 5, 123L, 456L
                )
        );
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("방 공개/비공개 여부와 비밀번호 설정이 일치하지 않습니다."));
        assertTrue(ex.getCause().getMessage().contains("공개 여부 = false, 비밀번호 존재 여부 = false"));
    }

    @Test
    @DisplayName("withoutId: 시간 순서상 시작일 -> 종료일이 아닐 경우, InvalidStateException 발생한다.")
    void withoutId_startDate_not_before_endDate() {
        LocalDate start = LocalDate.of(2025, 8, 1);
        LocalDate end = LocalDate.of(2025, 7, 1);
        InvalidStateException ex = assertThrows(InvalidStateException.class, () ->
                Room.withoutId(
                        "제목", "설명", false, "pass",
                        start, end, 5, 123L, 456L
                )
        );
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains(
                String.format("시작일(%s)은 종료일(%s)보다 이전이어야 합니다.", start, end)
        ));
    }

    @Test
    @DisplayName("withoutId: 시작일이 현재 날짜보다 이전일 경우, InvalidStateException 발생한다.")
    void withoutId_startDate_before_today() {
        LocalDate today = LocalDate.now();
        LocalDate past = today.minusDays(1);
        LocalDate future = today.plusDays(1);

        // 시작일이 현재시점 - 1 일
        InvalidStateException ex = assertThrows(InvalidStateException.class, () ->
                Room.withoutId(
                        "제목", "설명", false, "pass",
                        past, future, 5, 123L, 456L
                )
        );
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains(
                String.format("시작일(%s)은 현재 날짜(%s) 이후여야 합니다.", past, today)
        ));
    }

}
