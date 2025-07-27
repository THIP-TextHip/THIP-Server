package konkuk.thip.room.application.service.validator;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.room.application.port.out.RoomParticipantQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static konkuk.thip.common.exception.code.ErrorCode.ROOM_ACCESS_FORBIDDEN;

@Component
@RequiredArgsConstructor
public class RoomParticipantValidator{
    private final RoomParticipantQueryPort participantPort;

    // 사용자가 방에 속해있는지 검증
    public void validateUserIsRoomMember(Long roomId, Long userId) {
        if (!participantPort.existByUserIdAndRoomId(roomId, userId)) {
            throw new InvalidStateException(ROOM_ACCESS_FORBIDDEN,
                new IllegalArgumentException("사용자가 이 방의 참가자가 아닙니다. roomId=" + roomId + ", userId=" + userId));
        }
    }
}