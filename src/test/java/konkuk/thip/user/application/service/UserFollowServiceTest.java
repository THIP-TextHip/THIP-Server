package konkuk.thip.user.application.service;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.user.application.port.in.dto.UserFollowCommand;
import konkuk.thip.user.application.port.out.FollowingCommandPort;
import konkuk.thip.user.application.service.following.UserFollowService;
import konkuk.thip.user.domain.Following;
import konkuk.thip.common.entity.StatusType;
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

class UserFollowServiceTest {

    private FollowingCommandPort followingCommandPort;
    private UserFollowService userFollowService;

    @BeforeEach
    void setUp() {
        followingCommandPort = mock(FollowingCommandPort.class);
        userFollowService = new UserFollowService(followingCommandPort);
    }

    @Nested
    @DisplayName("팔로우 요청(type = true)")
    class Follow {

        @Test
        @DisplayName("기존 inactive row가 존재하면 active로 변경")
        void activate_existingFollowing() {
            // given
            Long userId = 1L, targetUserId = 2L;
            Following inactiveFollowing = Following.builder()
                    .id(10L)
                    .followerUserId(userId)
                    .followingUserId(targetUserId)
                    .status(StatusType.INACTIVE)
                    .build();

            when(followingCommandPort.findByUserIdAndTargetUserId(userId, targetUserId))
                    .thenReturn(Optional.of(inactiveFollowing));

            UserFollowCommand command = new UserFollowCommand(userId, targetUserId, true);

            // when
            Boolean result = userFollowService.changeFollowingState(command);

            // then
            assertThat(result).isTrue();
            assertThat(inactiveFollowing.getStatus()).isEqualTo(StatusType.ACTIVE);
            verify(followingCommandPort).updateStatus(inactiveFollowing);
        }

        @Test
        @DisplayName("팔로우 관계가 존재하지 않으면 새로 생성")
        void create_newFollowing() {
            // given
            Long userId = 1L, targetUserId = 2L;
            when(followingCommandPort.findByUserIdAndTargetUserId(userId, targetUserId))
                    .thenReturn(Optional.empty());

            UserFollowCommand command = new UserFollowCommand(userId, targetUserId, true);

            // when
            Boolean result = userFollowService.changeFollowingState(command);

            // then
            assertThat(result).isTrue();
            ArgumentCaptor<Following> captor = ArgumentCaptor.forClass(Following.class);
            verify(followingCommandPort).save(captor.capture());

            Following saved = captor.getValue();
            assertThat(saved.getFollowerUserId()).isEqualTo(userId);
            assertThat(saved.getFollowingUserId()).isEqualTo(targetUserId);
            assertThat(saved.getStatus()).isEqualTo(StatusType.ACTIVE);
        }
    }

    @Nested
    @DisplayName("언팔로우 요청(type = false)")
    class Unfollow {

        @Test
        @DisplayName("active row가 존재하면 inactive로 변경")
        void deactivate_existingFollowing() {
            // given
            Long userId = 1L, targetUserId = 2L;
            Following activeFollowing = Following.builder()
                    .id(10L)
                    .followerUserId(userId)
                    .followingUserId(targetUserId)
                    .status(StatusType.ACTIVE)
                    .build();

            when(followingCommandPort.findByUserIdAndTargetUserId(userId, targetUserId))
                    .thenReturn(Optional.of(activeFollowing));

            UserFollowCommand command = new UserFollowCommand(userId, targetUserId, false);

            // when
            Boolean result = userFollowService.changeFollowingState(command);

            // then
            assertThat(result).isFalse();
            assertThat(activeFollowing.getStatus()).isEqualTo(StatusType.INACTIVE);
            verify(followingCommandPort).updateStatus(activeFollowing);
        }

        @Test
        @DisplayName("언팔로우 요청인데 팔로우 관계가 없으면 예외 발생")
        void unfollow_withoutRelation() {
            // given
            Long userId = 1L, targetUserId = 2L;
            when(followingCommandPort.findByUserIdAndTargetUserId(userId, targetUserId))
                    .thenReturn(Optional.empty());

            UserFollowCommand command = new UserFollowCommand(userId, targetUserId, false);

            // when & then
            assertThatThrownBy(() -> userFollowService.changeFollowingState(command))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessageContaining(USER_ALREADY_UNFOLLOWED.getMessage());
        }
    }

    @Test
    @DisplayName("자기 자신을 팔로우하는 요청이면 예외 발생")
    void cannot_follow_self() {
        Long userId = 1L;
        UserFollowCommand command = new UserFollowCommand(userId, userId, true);

        assertThatThrownBy(() -> userFollowService.changeFollowingState(command))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining(USER_CANNOT_FOLLOW_SELF.getMessage());
    }
}