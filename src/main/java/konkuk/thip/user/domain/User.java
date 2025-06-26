package konkuk.thip.user.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class User extends BaseDomainEntity {

    private Long id;

    private String email;

    private String nickname;

    private String imageUrl;

    private String userRole;

    private Long aliasId;

    public static User withoutId(String email, String nickname, String imageUrl, String userRole, Long aliasId) {
        return User.builder()
                .id(null)
                .email(email)
                .nickname(nickname)
                .imageUrl(imageUrl)
                .userRole(userRole)
                .aliasId(aliasId)
                .build();
    }

}
