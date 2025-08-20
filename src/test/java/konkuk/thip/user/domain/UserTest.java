package konkuk.thip.user.domain;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.user.domain.value.Alias;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("[단위] User 단위 테스트")
class UserTest {

    private User user;
    private Alias alias;

    @BeforeEach
    void setUp() {
        alias = Alias.WRITER;
        user = User.builder()
                .id(1L)
                .nickname("thip")
                .nicknameUpdatedAt(LocalDate.now().minusMonths(7)) // 7개월 전
                .userRole("USER")
                .oauth2Id("oauth2-id")
                .followerCount(10)
                .alias(alias)
                .build();
    }

    @Test
    @DisplayName("닉네임 빈 값이면 예외 발생")
            //영어로 네이밍
    void nicknameCannotBeBlank_throwsException() {
        assertThatThrownBy(() -> user.updateUserInfo(" ", alias, true))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining(ErrorCode.USER_NICKNAME_CANNOT_BE_BLANK.getMessage());
    }

    @Test
    @DisplayName("닉네임 10자 초과이면 예외 발생")
    void nicknameTooLong_throwsException() {
        String longNickname = "thipthipthip"; // 12자
        assertThatThrownBy(() -> user.updateUserInfo(longNickname, alias, true))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining(ErrorCode.USER_NICKNAME_TOO_LONG.getMessage());
    }

    @Test
    @DisplayName("닉네임이 기존 닉네임과 같으면 예외 발생")
    void nicknameCannotBeSame_throwsException() {
        assertThatThrownBy(() -> user.updateUserInfo("thip", alias, true))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining(ErrorCode.USER_NICKNAME_CANNOT_BE_SAME.getMessage());
    }

    @Test
    @DisplayName("닉네임이 6개월 이내에 변경되면 예외 발생")
    void nicknameUpdateTooFrequent_throwsException() {
        user = User.builder()
                .nicknameUpdatedAt(LocalDate.now().minusMonths(3))
                .build();

        assertThatThrownBy(() -> user.updateUserInfo("newthip", alias, true))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining(ErrorCode.USER_NICKNAME_UPDATE_TOO_FREQUENT.getMessage());
    }

    @Test
    @DisplayName("닉네임과 별칭을 정상적으로 업데이트")
    void updateUserInfo_success() {
        Alias newAlias = Alias.ARTIST;
        String newNickname = "newNick";

        user.updateUserInfo(newNickname, newAlias, true);

        assertThat(user.getNickname()).isEqualTo(newNickname);
        assertThat(user.getAlias()).isEqualTo(newAlias);
        assertThat(user.getNicknameUpdatedAt()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("닉네임 업데이트 없이 별칭만 변경")
    void 닉네임_업데이트_없이_별칭만_변경() {
        Alias newAlias = Alias.SCIENTIST;

        user.updateUserInfo(null, newAlias, false);

        assertThat(user.getAlias()).isEqualTo(newAlias);
        assertThat(user.getNickname()).isEqualTo("thip");
    }
}