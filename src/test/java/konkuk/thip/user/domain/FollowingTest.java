package konkuk.thip.user.domain;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[단위] Following 단위 테스트")
class FollowingTest {

    @Nested
    @DisplayName("팔로우 요청인 경우")
    class FollowRequest {

        private final boolean isFollowRequest = true;

        @Test
        @DisplayName("이미 팔로우 중이면 예외 발생")
        void alreadyFollowed_shouldThrowException() {
            boolean isExistingFollowing = true;

            assertThatThrownBy(() ->
                    Following.validateFollowingState(isExistingFollowing, isFollowRequest))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessageContaining(ErrorCode.USER_ALREADY_FOLLOWED.getMessage());
        }

        @Test
        @DisplayName("팔로우 관계가 없으면 true 반환")
        void notFollowed_shouldReturnTrue() {
            boolean isExistingFollowing = false;

            boolean result = Following.validateFollowingState(isExistingFollowing, isFollowRequest);

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("언팔로우 요청인 경우")
    class UnfollowRequest {

        private final boolean isFollowRequest = false;

        @Test
        @DisplayName("팔로우 관계가 없으면 예외 발생")
        void notFollowed_shouldThrowException() {
            boolean isExistingFollowing = false;

            assertThatThrownBy(() ->
                    Following.validateFollowingState(isExistingFollowing, isFollowRequest))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessageContaining(ErrorCode.USER_ALREADY_UNFOLLOWED.getMessage());
        }

        @Test
        @DisplayName("팔로우 중이면 false 반환")
        void alreadyFollowed_shouldReturnFalse() {
            boolean isExistingFollowing = true;

            boolean result = Following.validateFollowingState(isExistingFollowing, isFollowRequest);

            assertThat(result).isFalse();
        }
    }
}