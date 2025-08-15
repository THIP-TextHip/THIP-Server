package konkuk.thip.roompost.domain;

import konkuk.thip.common.exception.InvalidStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("[단위] AttendanceCheck 도메인 테스트")
class AttendanceCheckTest {

    @Test
    @DisplayName("유저가 오늘 하루동안 이미 작성한 오늘의 한마디가 5개 미만일 경우, id가 null인 AttendanceCheck 도메인이 생성된다.")
    void withoutId_success_test() throws Exception {
        //given
        Long userId = 1L;
        Long roomId = 1L;
        String todayComment = "오늘의 한마디~~";
        int alreadyWrittenCountTodayOfUser = 3;

        //when
        AttendanceCheck attendanceCheck = AttendanceCheck.withoutId(
                roomId, userId, todayComment, alreadyWrittenCountTodayOfUser
        );

        //then
        assertNotNull(attendanceCheck);
        assertNull(attendanceCheck.getId());
        assertEquals(roomId, attendanceCheck.getRoomId());
        assertEquals(userId, attendanceCheck.getCreatorId());
        assertEquals(todayComment, attendanceCheck.getTodayComment());
    }

    @Test
    @DisplayName("유저가 오늘 이미 작성한 오늘의 한마디가 5개 이상이면 예외가 발생한다.")
    void withoutId_fail_when_write_limit_exceeded() {
        // given
        Long userId = 1L;
        Long roomId = 1L;
        String todayComment = "오늘의 한마디~~";
        int alreadyWrittenCountTodayOfUser = 5;

        // when // then
        assertThrows(InvalidStateException.class,
                () -> AttendanceCheck.withoutId(roomId, userId, todayComment, alreadyWrittenCountTodayOfUser));
    }
}
