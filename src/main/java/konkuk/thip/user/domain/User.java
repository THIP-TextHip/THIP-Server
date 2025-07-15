package konkuk.thip.user.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class User extends BaseDomainEntity {

    private Long id;

    private String nickname;

    private String userRole;

    private String oauth2Id;

    private Integer followingCount;

    private Alias alias;

    public static User withoutId(String nickname, String userRole, String oauth2Id, Alias alias) {
        return User.builder()
                .id(null)
                .nickname(nickname)
                .userRole(userRole)
                .oauth2Id(oauth2Id)
                .followingCount(0)
                .alias(alias)
                .build();
    }

}
