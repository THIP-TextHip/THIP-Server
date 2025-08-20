package konkuk.thip.room.domain;

import konkuk.thip.common.exception.InvalidStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static konkuk.thip.common.exception.code.ErrorCode.ROOM_HOST_CANNOT_LEAVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("[단위] RoomParticipant 도메인 테스트")
class RoomParticipantTest {

    @Test
    @DisplayName("canWriteOverview: 사용자 퍼센트가 80 이상일 때, true를 반환한다.")
    void can_writeOverview() {
        //given
        RoomParticipant roomParticipant = RoomParticipant.builder()
                .userPercentage(80.0)
                .build();

        //when
        boolean canWrite = roomParticipant.canWriteOverview();

        //then
        assertThat(canWrite).isTrue();
    }

    @Test
    @DisplayName("canWriteOverview: 사용자 퍼센트가 80 미만일 때, false를 반환한다.")
    void cannot_writeOverview() {
        //given
        RoomParticipant roomParticipant = RoomParticipant.builder()
                .userPercentage(79.9)
                .build();

        //when
        boolean canWrite = roomParticipant.canWriteOverview();

        //then
        assertThat(canWrite).isFalse();
    }

    @Test
    @DisplayName("updateUserProgress: 요청 페이지가 현재 페이지보다 클 때, 현재 페이지와 사용자 퍼센트가 업데이트된다.")
    void update_userProgress_success() throws Exception {
        //given
        RoomParticipant roomParticipant = RoomParticipant.builder()
                .currentPage(1)
                .userPercentage(5.0)
                .userId(1L)
                .roomId(1L)
                .build();

        //when
        int totalPageCount = 20;
        boolean isUpdated = roomParticipant.updateUserProgress(5, totalPageCount);

        //then
        assertThat(isUpdated).isTrue();
        assertThat(roomParticipant.getCurrentPage()).isEqualTo(5);
        double ratio = (double) roomParticipant.getCurrentPage() / totalPageCount;
        assertThat(roomParticipant.getUserPercentage()).isEqualTo(ratio * 100);
    }

    @Test
    @DisplayName("updateUserProgress: 요청 페이지가 현재 페이지보다 작을 때, 현재 페이지와 사용자 퍼센트가 업데이트되지 않는다.")
    void update_userProgress_when_request_isLower_than_bookPage() throws Exception {
        //given
        RoomParticipant roomParticipant = RoomParticipant.builder()
                .currentPage(5)
                .userPercentage(25.0)
                .userId(1L)
                .roomId(1L)
                .build();

        //when
        int totalPageCount = 20;
        boolean isUpdated = roomParticipant.updateUserProgress(3, totalPageCount);

        //then
        assertThat(isUpdated).isFalse();
        assertThat(roomParticipant.getCurrentPage()).isEqualTo(5);
        double ratio = (double) roomParticipant.getCurrentPage() / totalPageCount;
        assertThat(roomParticipant.getUserPercentage()).isEqualTo(ratio * 100);
    }

    @Test
    @DisplayName("validateRoomLeavable: 방의 HOST가 방을 나가려고 하면 InvalidStateException이 발생한다.")
    void host_cannot_leave_room_test() {
        // given: HOST 참여자 생성
        RoomParticipant host = RoomParticipant.hostWithoutId(1L,1L);

        // when & then
        InvalidStateException ex = assertThrows(InvalidStateException.class,
                host::validateRoomLeavable);
        assertEquals(ROOM_HOST_CANNOT_LEAVE, ex.getErrorCode());
    }

    @Test
    @DisplayName("MEMBER는 방을 나갈 수 있다.")
    void member_can_leave_room_test() {
        // given: MEMBER 참여자 생성
        RoomParticipant member = RoomParticipant.memberWithoutId(1L,1L);

        // when & then
        assertDoesNotThrow(member::validateRoomLeavable);
    }

}