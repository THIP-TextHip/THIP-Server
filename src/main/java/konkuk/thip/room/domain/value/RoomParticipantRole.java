
package konkuk.thip.room.domain.value;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public enum RoomParticipantRole {

    HOST("호스트"),
    MEMBER("팀원");

    private final String type;

    RoomParticipantRole(String type) {
        this.type = type;
    }

    public static RoomParticipantRole from(String type) {
        for (RoomParticipantRole roomParticipantRole : RoomParticipantRole.values()) {
            if (roomParticipantRole.getType().equals(type)) {
                return roomParticipantRole;
            }
        }
        throw new InvalidStateException(ErrorCode.ROOM_PARTICIPANT_ROLE_NOT_MATCH, new IllegalArgumentException("요청된 사용자 역할: " + type));
    }


}