
package konkuk.thip.room.adapter.out.jpa;

import lombok.Getter;

@Getter
public enum RoomParticipantRole {

    HOST("호스트"),
    MEMBER("팀원");

    private String type;

    RoomParticipantRole(String type) {
        this.type = type;
    }

    public static RoomParticipantRole from(String type) {
        for (RoomParticipantRole roomParticipantRole : RoomParticipantRole.values()) {
            if (roomParticipantRole.getType().equals(type)) {
                return roomParticipantRole;
            }
        }
        //컨트롤러 어드바이스 추가하고 예외처리
        //throw new GlobalException(NO_SUCH_TYPE_USER);
        return null;
    }


}