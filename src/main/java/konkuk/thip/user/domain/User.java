package konkuk.thip.user.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.user.domain.value.Alias;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@SuperBuilder
public class User extends BaseDomainEntity {

    private Long id;

    private String nickname;

    private LocalDate nicknameUpdatedAt;

    private String userRole;

    private String oauth2Id;

    private Integer followerCount; // 팔로워 수

    private Alias alias;

    public static User withoutId(String nickname, String userRole, String oauth2Id, Alias alias) {
        return User.builder()
                .id(null)
                .nickname(nickname)
                .nicknameUpdatedAt(null)
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
            throw new InvalidStateException(ErrorCode.FOLLOW_COUNT_CANNOT_BE_NEGATIVE);
        }
        followerCount--;
    }

    public void updateUserInfo(String nickname, Alias alias, boolean isNicknameUpdateRequest) {
        if(isNicknameUpdateRequest) {
            validateCanUpdateNickname(nickname);
            this.nickname = nickname;
            this.nicknameUpdatedAt = LocalDate.now();
        }
        this.alias = alias;
    }

    private void validateCanUpdateNickname(String nickname) {
        if(nickname.isBlank()) { // 빈칸 불가
            throw new InvalidStateException(ErrorCode.USER_NICKNAME_CANNOT_BE_BLANK);
        }
        if(nickname.length() > 10) { // 10자 이상 불가
            throw new InvalidStateException(ErrorCode.USER_NICKNAME_TOO_LONG);
        }
        // 닉네임을 변경한지 6개월이 지나지 않았으면 닉네임 업데이트 불가
        if(nicknameUpdatedAt != null && nicknameUpdatedAt.isAfter(LocalDate.now().minusMonths(6))) {
            throw new InvalidStateException(ErrorCode.USER_NICKNAME_UPDATE_TOO_FREQUENT);
        }
        if(nickname.equals(this.nickname)) { // 현재 닉네임과 같으면 업데이트 불가
            throw new InvalidStateException(ErrorCode.USER_NICKNAME_CANNOT_BE_SAME);
        }
    }

}
