package konkuk.thip.user.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class User extends BaseDomainEntity {

    private Long id;

    private String nickname;

    private String imageUrl;

    private String userRole;

    private Long aliasId;

    private String oauth2Id;

    public static User withoutId(String nickname, String imageUrl, String userRole, Long aliasId, String oauth2Id) {
        return User.builder()
                .id(null)
                .nickname(nickname)
                .imageUrl(imageUrl)
                .userRole(userRole)
                .aliasId(aliasId)
                .oauth2Id(oauth2Id)
                .build();
    }

}
