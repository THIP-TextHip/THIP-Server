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
                        0.0, START, END, 5, 123L, 456L
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
                        0.0, START, END, 5, 123L, 456L
                )
        );
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("방 공개/비공개 여부와 비밀번호 설정이 일치하지 않습니다."));
        assertTrue(ex.getCause().getMessage().contains("공개 여부 = false, 비밀번호 존재 여부 = false"));
    }
}
