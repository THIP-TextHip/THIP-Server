package konkuk.thip.user.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class User extends BaseDomainEntity {

    private Long id;

    private String nickname;

    private String userRole;

    private String oauth2Id;

    private Integer followerCount; // 팔로워 수

    private Alias alias;

    public static User withoutId(String nickname, String userRole, String oauth2Id, Alias alias) {
        return User.builder()
                .id(null)
                .nickname(nickname)
                .userRole(userRole)
                .oauth2Id(oauth2Id)
                .followerCount(0)
                .alias(alias)
                .build();
    }

    public void increaseFollowerCount() {
        followerCount++;
    }

    public void decreaseFollowerCount() {
        if(followerCount == 0) {
            throw new InvalidStateException(ErrorCode.FOLLOW_COUNT_IS_ZERO);
        }
        followerCount--;
    }

}
