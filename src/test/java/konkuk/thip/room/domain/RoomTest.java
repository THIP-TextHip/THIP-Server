package konkuk.thip.room.domain;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@DisplayName("[단위] Room 단위 테스트")
class RoomTest {

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private final LocalDate today = LocalDate.now();
    private final LocalDate START = today.plusDays(1);
    private final LocalDate END = today.plusDays(32);
    private final Category validCategory = Category.from("과학/IT");

    @Test
    @DisplayName("withoutId: 공개 방이면서 password가 not null 이면, InvalidStateException 발생한다.")
    void withoutId_public_password_not_null() {
        InvalidStateException ex = assertThrows(InvalidStateException.class, () ->
                Room.withoutId(
                        "제목", "설명", true, "1234",
                        START, END, 5, 123L, validCategory
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
                        START, END, 5, 123L, validCategory
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
                        start, end, 5, 123L, validCategory
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
                        past, future, 5, 123L, validCategory
                )
        );
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains(
                String.format("시작일(%s)은 현재 날짜(%s) 이후여야 합니다.", past, today)
        ));
    }

    @Test
    @DisplayName("withoutId: 시작일 현재 날짜와 동일할 경우, InvalidStateException 발생한다.")
    void withoutId_startDate_is_today() {
        LocalDate today = LocalDate.now();
        LocalDate future = today.plusDays(1);

        // 시작일 == 현재시점
        InvalidStateException ex = assertThrows(InvalidStateException.class, () ->
                Room.withoutId(
                        "제목", "설명", false, "1234",
                        today, future, 5, 123L, validCategory
                )
        );
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains(
                String.format("시작일(%s)은 현재 날짜(%s) 이후여야 합니다.", today, today)
        ));
    }

    @Test
    @DisplayName("withoutId: 전달받은 비밀번호를 해싱해서 보관한다.")
    void withoutId_password_hashing() {
        String rawPassword = "1234";

        Room room = Room.withoutId(
                "제목", "설명", false, rawPassword,
                START, END, 5, 123L, validCategory
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
                START, END, 5, 123L, validCategory
        );
        assertTrue(room.matchesPassword("1234"));
    }

    @Test
    @DisplayName("matchesPassword: 잘못된 비밀번호면 false 반환")
    void matchesPassword_incorrect_password() {
        Room room = Room.withoutId(
                "제목", "설명", false, "1234",
                START, END, 5, 123L, validCategory
        );
        assertFalse(room.matchesPassword("0000"));
    }

    @Test
    @DisplayName("matchesPassword: Room이 비밀번호가 설정되어 있지 않은 공개방일 경우, false 반환")
    void matchesPassword_no_password() {
        Room room = Room.withoutId(
                "제목", "설명", true, null,
                START, END, 5, 123L, validCategory
        );
        assertFalse(room.matchesPassword("0000"));
    }

    @Test
    @DisplayName("isRecruitmentPeriodExpired: 모집기간이 만료되지 않은 경우 false 반환")
    void isRecruitmentPeriodExpired_not_expired() {
        Room room = Room.withoutId(
                "제목", "설명", false, "1234",
                LocalDate.now().plusDays(3), LocalDate.now().plusDays(10), 5, 123L, validCategory
        );
        assertFalse(room.isRecruitmentPeriodExpired());
    }

    @Test
    @DisplayName("isRecruitmentPeriodExpired: 모집기간이 오늘이 마감일인 경우 false 반환")
    void isRecruitmentPeriodExpired_deadline_today() {
        LocalDate start = LocalDate.now().plusDays(1);
        Room room = Room.withoutId(
                "제목", "설명", false, "1234",
                start, start.plusDays(10), 5, 123L, validCategory
        );
        // 오늘이 모집마감일(startDate.minusDays(1))이면 false
        assertFalse(room.isRecruitmentPeriodExpired());
    }

    @Test
    @DisplayName("isRecruitmentPeriodExpired: 모집기간이 이미 만료된 경우 true 반환")
    void isRecruitmentPeriodExpired_expired() {
        LocalDate start = LocalDate.now().plusDays(1);
        Room room = Room.withoutId(
                "제목", "설명", false, "1234",
                start, start.plusDays(10), 5, 123L, validCategory
        );
        setField(room, "startDate", today); // 모집기간 만료 상태를 강제로 만든 후 검증
        assertTrue(room.isRecruitmentPeriodExpired());
    }

    @Test
    @DisplayName("verifyPassword: 모집기간 만료 시 InvalidStateException(ROOM_RECRUITMENT_PERIOD_EXPIRED) 발생")
    void verifyPassword_recruitmentPeriodExpired() {
        LocalDate startExpired = today.plusDays(2);
        Room room = Room.withoutId(
                "제목", "설명", false, "1234",
                startExpired, END, 5, 123L, validCategory
        );
        setField(room, "startDate", today); // 모집기간 만료 상태를 강제로 만든 후 검증
        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> room.verifyPassword("1234"));
        assertEquals(ErrorCode.ROOM_RECRUITMENT_PERIOD_EXPIRED, ex.getErrorCode());
        assertTrue(ex.getCause().getMessage().contains("모집기간"));
    }

    @Test
    @DisplayName("verifyPassword: 공개방에 비밀번호 입력 시 InvalidStateException(ROOM_PASSWORD_NOT_REQUIRED) 발생")
    void verifyPassword_publicRoom() {
        Room room = Room.withoutId(
                "제목", "설명", true, null,
                START, END, 5, 123L, validCategory
        );
        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> room.verifyPassword("1234"));
        assertEquals(ErrorCode.ROOM_PASSWORD_NOT_REQUIRED, ex.getErrorCode());
    }

    @Test
    @DisplayName("verifyPassword: 비밀번호 불일치 시 BusinessException(ROOM_PASSWORD_MISMATCH) 발생")
    void verifyPassword_passwordMismatch() {
        Room room = Room.withoutId(
                "제목", "설명", false, "1234",
                START, END, 5, 123L, validCategory
        );
        BusinessException ex = assertThrows(BusinessException.class,
                () -> room.verifyPassword("0000"));
        assertEquals(ErrorCode.ROOM_PASSWORD_MISMATCH, ex.getErrorCode());
    }

    @Test
    @DisplayName("verifyPassword: 모집기간 내, 비공개방, 비밀번호 일치 시 예외 발생하지 않음")
    void verifyPassword_success() {
        Room room = Room.withoutId(
                "제목", "설명", false, "1234",
                START, END, 5, 123L, validCategory
        );
        assertDoesNotThrow(() -> room.verifyPassword("1234"));
    }

    @Test
    @DisplayName("increaseMemberCount: 정원이 다 찬 상태에서 호출하면 InvalidStateException 발생")
    void increaseMemberCount_exceed_limit() {
        Room room = Room.withoutId(
                "제목", "설명", false, "1234",
                START, END, 1, 123L, validCategory // recruitCount = 1 (방장 포함)
        );
        // 이미 memberCount = 1 이므로 더 이상 참여 불가
        InvalidStateException ex = assertThrows(InvalidStateException.class, room::increaseMemberCount);
        assertEquals(ErrorCode.ROOM_MEMBER_COUNT_EXCEEDED, ex.getErrorCode());
    }

    @Test
    @DisplayName("decreaseMemberCount: 인원이 1명(방장만 존재)일 때 호출하면 InvalidStateException 발생")
    void decreaseMemberCount_underflow() {
        Room room = Room.withoutId(
                "제목", "설명", false, "1234",
                START, END, 5, 123L, validCategory
        );
        // memberCount = 1 인 상태에서 감소 시도
        InvalidStateException ex = assertThrows(InvalidStateException.class, room::decreaseMemberCount);
        assertEquals(ErrorCode.ROOM_MEMBER_COUNT_UNDERFLOW, ex.getErrorCode());
    }

    @Test
    @DisplayName("정상적으로 모집 마감 시 startDate가 오늘로 변경된다")
    void startRoomProgress_success() {
        Room room = Room.withoutId(
                "방 제목", "방 설명", true, null,
                START, END, 5, 1L, validCategory
        );

        room.startRoomProgress();

        assertEquals(today, room.getStartDate());
    }

    @Test
    @DisplayName("모집 기간이 만료된 방을 모집 마감하려고 하면 InvalidStateException 발생")
    void startRoomProgress_recruitmentExpired() {
        Room room = Room.withoutId(
                "방 제목", "방 설명", true, null,
                today.plusDays(1), END, 5, 1L, validCategory
        );

        // 강제로 모집기간 만료된 상태로 변경
        room.startRoomProgress(); // startDate = 오늘

        InvalidStateException ex = assertThrows(InvalidStateException.class, room::startRoomProgress);
        assertEquals(ErrorCode.ROOM_RECRUITMENT_PERIOD_EXPIRED, ex.getErrorCode());
    }

}
