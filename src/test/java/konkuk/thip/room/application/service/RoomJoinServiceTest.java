package konkuk.thip.room.application.service;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.room.application.port.in.dto.RoomJoinCommand;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.application.port.out.RoomParticipantQueryPort;
import konkuk.thip.room.domain.Room;
import konkuk.thip.room.domain.RoomParticipant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static konkuk.thip.room.adapter.out.jpa.RoomParticipantRole.MEMBER;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@DisplayName("[단위] 방 참여/취소 서비스 단위 테스트")
class RoomJoinServiceTest {
    private RoomParticipantQueryPort roomParticipantQueryPort;
    private RoomCommandPort roomCommandPort;
    private RoomParticipantCommandPort roomParticipantCommandPort;
    private RoomJoinService roomJoinService;

    private final Long ROOM_ID = 1L;
    private final Long USER_ID = 2L;
    private final Room room = Room.withoutId("제목", "설명", true, null,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(30),
            5, 100L, null);

    @BeforeEach
    void setUp() {
        roomParticipantQueryPort = mock(RoomParticipantQueryPort.class);
        roomCommandPort = mock(RoomCommandPort.class);
        roomParticipantCommandPort = mock(RoomParticipantCommandPort.class);

        roomJoinService = new RoomJoinService(
                roomParticipantQueryPort,
                roomCommandPort,
                roomParticipantCommandPort
        );
    }

    @Nested
    @DisplayName("참여하기 요청")
    class Join {

        @Test
        @DisplayName("이미 참여한 경우 예외 발생")
        void alreadyParticipated() {
            RoomJoinCommand command = new RoomJoinCommand(USER_ID, ROOM_ID, "join");

            given(roomCommandPort.findById(ROOM_ID)).willReturn(room);
            given(roomParticipantQueryPort.existByUserIdAndRoomId(USER_ID, ROOM_ID)).willReturn(true);

            assertThatThrownBy(() -> roomJoinService.changeJoinState(command))
                    .isInstanceOf(InvalidStateException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_ALREADY_PARTICIPATE);
        }

        @Test
        @DisplayName("정상적으로 참여 시 참여자 저장 및 인원수 증가")
        void successJoin() {
            RoomJoinCommand command = new RoomJoinCommand(USER_ID, ROOM_ID, "join");

            given(roomCommandPort.findById(ROOM_ID)).willReturn(room);
            given(roomParticipantQueryPort.existByUserIdAndRoomId(USER_ID, ROOM_ID)).willReturn(false);

            roomJoinService.changeJoinState(command);

            then(roomParticipantCommandPort).should().save(any(RoomParticipant.class));
            then(roomCommandPort).should().updateMemberCount(any(Room.class));
        }
    }

    @Nested
    @DisplayName("취소하기 요청")
    class Cancel {

        @Test
        @DisplayName("참여하지 않은 경우 예외 발생")
        void notParticipated() {
            RoomJoinCommand command = new RoomJoinCommand(USER_ID, ROOM_ID, "cancel");

            given(roomCommandPort.findById(ROOM_ID)).willReturn(room);
            given(roomParticipantQueryPort.existByUserIdAndRoomId(USER_ID, ROOM_ID)).willReturn(false);

            assertThatThrownBy(() -> roomJoinService.changeJoinState(command))
                    .isInstanceOf(InvalidStateException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_PARTICIPATED_CANNOT_CANCEL);
        }

        @Test
        @DisplayName("정상적으로 취소 시 참여자 제거 및 인원수 감소")
        void successCancel() {
            RoomJoinCommand command = new RoomJoinCommand(USER_ID, ROOM_ID, "cancel");
            RoomParticipant participant = RoomParticipant.withoutId(USER_ID, ROOM_ID, MEMBER.getType());

            given(roomCommandPort.findById(ROOM_ID)).willReturn(room);
            given(roomParticipantQueryPort.existByUserIdAndRoomId(USER_ID, ROOM_ID)).willReturn(true);
            given(roomParticipantCommandPort.findByUserIdAndRoomId(USER_ID, ROOM_ID)).willReturn(participant);

            room.increaseMemberCount(); // 현재 2명 이상으로 만들어 줌

            roomJoinService.changeJoinState(command);

            then(roomParticipantCommandPort).should().deleteByUserIdAndRoomId(USER_ID, ROOM_ID);
            then(roomCommandPort).should().updateMemberCount(any(Room.class));
        }
    }

}