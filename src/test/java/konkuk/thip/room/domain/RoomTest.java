package konkuk.thip.room.domain;

import konkuk.thip.common.exception.InvalidStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Room 단위 테스트")
class RoomTest {

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final LocalDate today = LocalDate.now();
    private final LocalDate START = today.plusDays(1);
    private final LocalDate END = today.plusDays(32);

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
                        "제목", "설명", false, "1234",
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
                        "제목", "설명", false, "1234",
                        past, future, 5, 123L, 456L
                )
        );
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains(
                String.format("시작일(%s)은 현재 날짜(%s) 이후여야 합니다.", past, today)
        ));
    }

    @Test
    @DisplayName("withoutId: 전달받은 비밀번호를 해싱해서 보관한다.")
    void withoutId_password_hashing() {
        String rawPassword = "1234";

        Room room = Room.withoutId(
                "제목", "설명", false, rawPassword,
                START, END, 5, 123L, 456L
        );

        String hashed = room.getHashedPassword();

        // 해시된 비밀번호가 null이 아니고 원문과 다름
        assertNotNull(hashed);
        assertNotEquals(rawPassword, hashed);

        // matchesPassword를 통해 원문 검증 시 true
        assertTrue(PASSWORD_ENCODER.matches(rawPassword, hashed));
    }

    @Test
    @DisplayName("matchesPassword: 올바른 비밀번호면 true 반환")
    void matchesPassword_correct_password() {
        Room room = Room.withoutId(
                "제목", "설명", false, "1234",
                START, END, 5, 123L, 456L
        );
        assertTrue(room.matchesPassword("1234"));
    }

    @Test
    @DisplayName("matchesPassword: 잘못된 비밀번호면 false 반환")
    void matchesPassword_incorrect_password() {
        Room room = Room.withoutId(
                "제목", "설명", false, "1234",
                START, END, 5, 123L, 456L
        );
        assertFalse(room.matchesPassword("0000"));
    }

    @Test
    @DisplayName("matchesPassword: Room이 비밀번호가 설정되어 있지 않은 공개방일 경우, false 반환")
    void matchesPassword_no_password() {
        Room room = Room.withoutId(
                "제목", "설명", true, null,
                START, END, 5, 123L, 456L
        );
        assertFalse(room.matchesPassword("0000"));
    }
}
