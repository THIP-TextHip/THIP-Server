package konkuk.thip.user.application.service;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.user.application.port.in.dto.UserBlockCommand;
import konkuk.thip.user.application.port.out.FollowingCommandPort;
import konkuk.thip.user.application.port.out.UserBlockCommandPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.application.service.block.UserBlockService;
import konkuk.thip.user.domain.Following;
import konkuk.thip.user.domain.User;
import konkuk.thip.user.domain.UserBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("[단위] UserBlockService 단위 테스트")
class UserBlockServiceTest {

    private UserBlockCommandPort userBlockCommandPort;
    private FollowingCommandPort followingCommandPort;
    private UserCommandPort userCommandPort;
    private UserBlockService userBlockService;

    @BeforeEach
    void setUp() {
        userBlockCommandPort = mock(UserBlockCommandPort.class);
        followingCommandPort = mock(FollowingCommandPort.class);
        userCommandPort = mock(UserCommandPort.class);
        userBlockService = new UserBlockService(userBlockCommandPort, followingCommandPort, userCommandPort);
    }

    @Nested
    @DisplayName("차단 요청(type = true)")
    class Block {

        @Test
        @DisplayName("차단 관계가 없으면 차단을 저장하고 true 를 반환한다.")
        void block_newRelation() {
            // given
            Long userId = 1L, targetUserId = 2L;
            when(userBlockCommandPort.findByUserIdAndTargetUserId(userId, targetUserId)).thenReturn(Optional.empty());
            when(userCommandPort.findByIdWithLock(userId)).thenReturn(createUserWithFollowerCount(userId, 0));
            when(userCommandPort.findByIdWithLock(targetUserId)).thenReturn(createUserWithFollowerCount(targetUserId, 0));
            when(followingCommandPort.findByUserIdAndTargetUserId(any(), any())).thenReturn(Optional.empty());

            // when
            Boolean result = userBlockService.changeBlockState(new UserBlockCommand(userId, targetUserId, true));

            // then
            assertThat(result).isTrue();
            ArgumentCaptor<UserBlock> captor = ArgumentCaptor.forClass(UserBlock.class);
            verify(userBlockCommandPort).save(captor.capture());
            assertThat(captor.getValue().getUserId()).isEqualTo(userId);
            assertThat(captor.getValue().getBlockedUserId()).isEqualTo(targetUserId);
        }

        @Test
        @DisplayName("양방향 팔로우가 존재하면 둘 다 해제하고 팔로워 수를 감소시킨다.")
        void block_unfollowsBothWays() {
            // given
            Long userId = 1L, targetUserId = 2L;
            User user = createUserWithFollowerCount(userId, 1);
            User target = createUserWithFollowerCount(targetUserId, 1);

            when(userBlockCommandPort.findByUserIdAndTargetUserId(userId, targetUserId)).thenReturn(Optional.empty());
            when(userCommandPort.findByIdWithLock(userId)).thenReturn(user);
            when(userCommandPort.findByIdWithLock(targetUserId)).thenReturn(target);
            when(followingCommandPort.findByUserIdAndTargetUserId(userId, targetUserId))
                    .thenReturn(Optional.of(Following.withoutId(userId, targetUserId)));
            when(followingCommandPort.findByUserIdAndTargetUserId(targetUserId, userId))
                    .thenReturn(Optional.of(Following.withoutId(targetUserId, userId)));

            // when
            userBlockService.changeBlockState(new UserBlockCommand(userId, targetUserId, true));

            // then
            verify(followingCommandPort, times(2)).deleteFollowing(any(Following.class), any(User.class));
            assertThat(user.getFollowerCount()).isZero();
            assertThat(target.getFollowerCount()).isZero();
        }

        @Test
        @DisplayName("이미 차단한 사용자를 다시 차단하면 예외가 발생한다.")
        void block_alreadyBlocked() {
            // given
            Long userId = 1L, targetUserId = 2L;
            when(userBlockCommandPort.findByUserIdAndTargetUserId(userId, targetUserId))
                    .thenReturn(Optional.of(UserBlock.withoutId(userId, targetUserId)));

            // when & then
            assertThatThrownBy(() -> userBlockService.changeBlockState(new UserBlockCommand(userId, targetUserId, true)))
                    .isInstanceOf(InvalidStateException.class);
            verify(userBlockCommandPort, never()).save(any());
        }

        @Test
        @DisplayName("자기 자신을 차단하면 예외가 발생한다.")
        void block_self() {
            assertThatThrownBy(() -> userBlockService.changeBlockState(new UserBlockCommand(1L, 1L, true)))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("차단 해제 요청(type = false)")
    class Unblock {

        @Test
        @DisplayName("차단을 해제하면 차단 관계만 삭제하고 팔로우는 복구하지 않는다.")
        void unblock_doesNotRestoreFollow() {
            // given
            Long userId = 1L, targetUserId = 2L;
            when(userBlockCommandPort.findByUserIdAndTargetUserId(userId, targetUserId))
                    .thenReturn(Optional.of(UserBlock.withoutId(userId, targetUserId)));

            // when
            Boolean result = userBlockService.changeBlockState(new UserBlockCommand(userId, targetUserId, false));

            // then
            assertThat(result).isFalse();
            verify(userBlockCommandPort).deleteBlock(any(UserBlock.class));
            verify(followingCommandPort, never()).save(any(), any());
            verify(userCommandPort, never()).findByIdWithLock(any());
        }

        @Test
        @DisplayName("차단하지 않은 사용자를 해제하면 예외가 발생한다.")
        void unblock_notBlocked() {
            // given
            Long userId = 1L, targetUserId = 2L;
            when(userBlockCommandPort.findByUserIdAndTargetUserId(userId, targetUserId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userBlockService.changeBlockState(new UserBlockCommand(userId, targetUserId, false)))
                    .isInstanceOf(InvalidStateException.class);
            verify(userBlockCommandPort, never()).deleteBlock(any());
        }
    }

    private User createUserWithFollowerCount(Long id, int followerCount) {
        return User.builder()
                .id(id)
                .nickname("테스터" + id)
                .userRole("USER")
                .oauth2Id("kakao_" + id)
                .followerCount(followerCount)
                .recordReviewCount(0)
                .build();
    }
}
