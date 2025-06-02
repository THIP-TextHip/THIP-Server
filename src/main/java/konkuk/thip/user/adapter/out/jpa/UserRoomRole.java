
package konkuk.thip.user.adapter.out.jpa;

import lombok.Getter;

@Getter
public enum UserRoomRole {

    HOST("호스트"),
    MEMBER("팀원");

    private String type;

    UserRoomRole(String type) {
        this.type = type;
    }

    public static UserRoomRole from(String type) {
        for (UserRoomRole userRoomRole : UserRoomRole.values()) {
            if (userRoomRole.getType().equals(type)) {
                return userRoomRole;
            }
        }
        //컨트롤러 어드바이스 추가하고 예외처리
        //throw new GlobalException(NO_SUCH_TYPE_USER);
        return null;
    }


}