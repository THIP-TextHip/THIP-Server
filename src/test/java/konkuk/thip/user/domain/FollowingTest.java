package konkuk.thip.user.domain;

import konkuk.thip.common.entity.StatusType;
import konkuk.thip.common.exception.InvalidStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static konkuk.thip.common.exception.code.ErrorCode.USER_ALREADY_FOLLOWED;
import static konkuk.thip.common.exception.code.ErrorCode.USER_ALREADY_UNFOLLOWED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FollowingTest {

    @Nested
    @DisplayName("팔로우 요청")
    class Follow {

        @Test
        @DisplayName("inactive 상태에서 follow 요청 → active로 변경")
        void follow_from_inactive() {
            Following following = Following.builder()
                    .followerUserId(1L)
                    .followingUserId(2L)
                    .status(StatusType.INACTIVE)
                    .build();

            boolean result = following.changeFollowingState(true);

            assertThat(result).isTrue();
            assertThat(following.getStatus()).isEqualTo(StatusType.ACTIVE);
        }

        @Test
        @DisplayName("이미 active 상태에서 follow 요청 → 예외 발생")
        void follow_from_active_should_throw() {
            Following following = Following.builder()
                    .followerUserId(1L)
                    .followingUserId(2L)
                    .status(StatusType.ACTIVE)
                    .build();

            assertThatThrownBy(() -> following.changeFollowingState(true))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessage(USER_ALREADY_FOLLOWED.getMessage());
        }
    }

    @Nested
    @DisplayName("언팔로우 요청")
    class Unfollow {

        @Test
        @DisplayName("active 상태에서 unfollow 요청 → inactive로 변경")
        void unfollow_from_active() {
            Following following = Following.builder()
                    .followerUserId(1L)
                    .followingUserId(2L)
                    .status(StatusType.ACTIVE)
                    .build();

            boolean result = following.changeFollowingState(false);

            assertThat(result).isFalse();
            assertThat(following.getStatus()).isEqualTo(StatusType.INACTIVE);
        }

        @Test
        @DisplayName("이미 inactive 상태에서 unfollow 요청 → 예외 발생")
        void unfollow_from_inactive_should_throw() {
            Following following = Following.builder()
                    .followerUserId(1L)
                    .followingUserId(2L)
                    .status(StatusType.INACTIVE)
                    .build();

            assertThatThrownBy(() -> following.changeFollowingState(false))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessage(USER_ALREADY_UNFOLLOWED.getMessage());
        }
    }

    @Test
    @DisplayName("새로운 팔로우 생성 시 상태는 ACTIVE")
    void create_following_should_be_active() {
        Following following = Following.withoutId(1L, 2L);

        assertThat(following.getFollowerUserId()).isEqualTo(1L);
        assertThat(following.getFollowingUserId()).isEqualTo(2L);
        assertThat(following.getStatus()).isEqualTo(StatusType.ACTIVE);
    }
}