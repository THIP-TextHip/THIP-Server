
package konkuk.thip.user.adapter.out.jpa;

import lombok.Getter;

@Getter
public enum UserRole {

    USER("일반유저"),
    INFLUENCER("인플루언서");

    private String type;

    UserRole(String type) {
        this.type = type;
    }

    public static UserRole from(String type) {
        for (UserRole userRole : UserRole.values()) {
            if (userRole.getType().equals(type)) {
                return userRole;
            }
        }
        //컨트롤러 어드바이스 추가하고 예외처리
        //throw new GlobalException(NO_SUCH_TYPE_USER);
        return null;
    }


}