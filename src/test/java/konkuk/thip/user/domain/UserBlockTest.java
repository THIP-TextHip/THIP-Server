package konkuk.thip.user.domain;

import konkuk.thip.common.entity.StatusType;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("[단위] UserBlock 도메인 테스트")
class UserBlockTest {

    @Test
    @DisplayName("withoutId 로 생성하면 ACTIVE 상태의 차단 관계가 만들어진다.")
    void withoutId_creates_active_block() {
        UserBlock userBlock = UserBlock.withoutId(1L, 2L);

        assertThat(userBlock.getUserId()).isEqualTo(1L);
        assertThat(userBlock.getBlockedUserId()).isEqualTo(2L);
        assertThat(userBlock.getStatus()).isEqualTo(StatusType.ACTIVE);
    }

    @Nested
    @DisplayName("validateBlockState")
    class ValidateBlockState {

        @Test
        @DisplayName("차단 관계가 없는 상태에서 차단 요청하면 true 를 반환한다.")
        void block_request_when_not_blocked() {
            assertThat(UserBlock.validateBlockState(false, true)).isTrue();
        }

        @Test
        @DisplayName("차단 관계가 있는 상태에서 해제 요청하면 false 를 반환한다.")
        void unblock_request_when_blocked() {
            assertThat(UserBlock.validateBlockState(true, false)).isFalse();
        }

        @Test
        @DisplayName("이미 차단한 사용자를 다시 차단하면 예외가 발생한다.")
        void block_request_when_already_blocked() {
            assertThatThrownBy(() -> UserBlock.validateBlockState(true, true))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessageContaining(ErrorCode.USER_ALREADY_BLOCKED.getMessage());
        }

        @Test
        @DisplayName("차단하지 않은 사용자를 해제하면 예외가 발생한다.")
        void unblock_request_when_not_blocked() {
            assertThatThrownBy(() -> UserBlock.validateBlockState(false, false))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessageContaining(ErrorCode.USER_ALREADY_UNBLOCKED.getMessage());
        }
    }
}
