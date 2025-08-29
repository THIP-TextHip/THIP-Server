package konkuk.thip.room.application.service.validator;

import konkuk.thip.common.annotation.application.HelperService;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.room.application.port.out.RoomParticipantQueryPort;
import lombok.RequiredArgsConstructor;

import static konkuk.thip.common.exception.code.ErrorCode.ROOM_ACCESS_FORBIDDEN;

@HelperService
@RequiredArgsConstructor
public class RoomParticipantValidator{
    private final RoomParticipantQueryPort participantPort;

    // 사용자가 방에 속해있는지 검증
    public void validateUserIsRoomMember(Long roomId, Long userId) {
        if (!participantPort.existByUserIdAndRoomId(userId, roomId)) {
            throw new InvalidStateException(ROOM_ACCESS_FORBIDDEN,
                new IllegalArgumentException("사용자가 이 방의 참가자가 아닙니다. roomId=" + roomId + ", userId=" + userId));
        }
    }
}