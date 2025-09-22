package konkuk.thip.user.application.service;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.notification.application.service.FeedNotificationOrchestratorSyncImpl;
import konkuk.thip.user.application.port.in.dto.UserFollowCommand;
import konkuk.thip.user.application.port.out.FollowingCommandPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.application.service.following.UserFollowService;
import konkuk.thip.user.domain.Following;
import konkuk.thip.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.USER_ALREADY_UNFOLLOWED;
import static konkuk.thip.common.exception.code.ErrorCode.USER_CANNOT_FOLLOW_SELF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("[단위] UserFollowService 단위 테스트")
class UserFollowServiceTest {

    private FollowingCommandPort followingCommandPort;
    private UserCommandPort userCommandPort;
    private UserFollowService userFollowService;

    private FeedNotificationOrchestratorSyncImpl feedNotificationOrchestratorSyncImpl;

    @BeforeEach
    void setUp() {
        followingCommandPort = mock(FollowingCommandPort.class);
        userCommandPort = mock(UserCommandPort.class);
        feedNotificationOrchestratorSyncImpl = mock(FeedNotificationOrchestratorSyncImpl.class);
        userFollowService = new UserFollowService(followingCommandPort, userCommandPort, feedNotificationOrchestratorSyncImpl);
    }

    @Nested
    @DisplayName("팔로우 요청(type = true)")
    class Follow {

        @Test
        @DisplayName("팔로우 관계가 이미 존재하면 예외 발생")
        void follow_alreadyExists() {
            // given
            Long userId = 1L, targetUserId = 2L;
            Following existing = Following.withoutId(userId, targetUserId);
            when(followingCommandPort.findByUserIdAndTargetUserId(userId, targetUserId))
                    .thenReturn(Optional.of(existing));

            User user = createUserWithFollowerCount(0);
            when(userCommandPort.findById(targetUserId)).thenReturn(user);

            UserFollowCommand command = new UserFollowCommand(userId, targetUserId, true);

            // then
            assertThatThrownBy(() -> userFollowService.changeFollowingState(command))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("팔로우 관계가 없으면 새로 생성 + followerCount 증가")
        void follow_newRelation() {
            // given
            Long userId = 1L, targetUserId = 2L;
            when(followingCommandPort.findByUserIdAndTargetUserId(userId, targetUserId))
                    .thenReturn(Optional.empty());

            User user = createUserWithFollowerCount(0);
            when(userCommandPort.findById(targetUserId)).thenReturn(user);
            when(userCommandPort.findById(userId)).thenReturn(user); // 알림 전송용

            UserFollowCommand command = new UserFollowCommand(userId, targetUserId, true);

            // when
            Boolean result = userFollowService.changeFollowingState(command);

            // then
            assertThat(result).isTrue();
            assertThat(user.getFollowerCount()).isEqualTo(1); // followerCount 증가

            ArgumentCaptor<Following> captor = ArgumentCaptor.forClass(Following.class);
            verify(followingCommandPort).save(captor.capture(), eq(user));
            Following saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(userId);
            assertThat(saved.getFollowingUserId()).isEqualTo(targetUserId);
        }
    }

    @Nested
    @DisplayName("언팔로우 요청(type = false)")
    class Unfollow {

        @Test
        @DisplayName("팔로우 관계가 존재하면 삭제 + followerCount 감소")
        void unfollow_existingRelation() {
            // given
            Long userId = 1L, targetUserId = 2L;
            Following existing = Following.withoutId(userId, targetUserId);

            User user = createUserWithFollowerCount(1);

            when(followingCommandPort.findByUserIdAndTargetUserId(userId, targetUserId))
                    .thenReturn(Optional.of(existing));
            when(userCommandPort.findById(targetUserId)).thenReturn(user);

            UserFollowCommand command = new UserFollowCommand(userId, targetUserId, false);

            // when
            Boolean result = userFollowService.changeFollowingState(command);

            // then
            assertThat(result).isFalse();
            assertThat(user.getFollowerCount()).isEqualTo(0); // followerCount 감소
            verify(followingCommandPort).deleteFollowing(existing, user);
        }

        @Test
        @DisplayName("언팔로우 요청인데 팔로우 관계가 없으면 예외 발생")
        void unfollow_withoutRelation() {
            // given
            Long userId = 1L, targetUserId = 2L;
            when(followingCommandPort.findByUserIdAndTargetUserId(userId, targetUserId))
                    .thenReturn(Optional.empty());

            UserFollowCommand command = new UserFollowCommand(userId, targetUserId, false);

            // then
            assertThatThrownBy(() -> userFollowService.changeFollowingState(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(USER_ALREADY_UNFOLLOWED.getMessage());
        }
    }

    @Test
    @DisplayName("자기 자신을 팔로우하는 요청이면 예외 발생")
    void cannot_follow_self() {
        Long userId = 1L;
        UserFollowCommand command = new UserFollowCommand(userId, userId, true);

        assertThatThrownBy(() -> userFollowService.changeFollowingState(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(USER_CANNOT_FOLLOW_SELF.getMessage());
    }

    private User createUserWithFollowerCount(int count) {
        return User.builder()
                .id(100L)
                .nickname("tester")
                .userRole("USER")
                .oauth2Id("oauth-id")
                .followerCount(count)
                .alias(null)
                .build();
    }
}
