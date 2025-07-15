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

    private Integer followingCount; // 팔로잉 수

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

    public void updateFollowingCount(boolean isFollowing) {
        if (isFollowing) {
            increaseFollowingCount();
        } else {
            decreaseFollowingCount();
        }
    }

    public void increaseFollowingCount() {
        followingCount++;
    }

    private void decreaseFollowingCount() {
        if(followingCount == 0) {
            throw new InvalidStateException(ErrorCode.FOLLOW_COUNT_IS_ZERO);
        }
        followingCount--;
    }

}
