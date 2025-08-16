package konkuk.thip.roompost.domain;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DisplayName("[단위] VoteParticipant 도메인 테스트")
class VoteParticipantTest {

    @Test
    @DisplayName("같은 voteItemId로 변경 시 예외 발생")
    void changeVoteItem_throwException_whenVoteItemIdIsSame() {
        // given
        Long userId = 1L;
        Long originalVoteItemId = 10L;
        VoteParticipant voteParticipant = VoteParticipant.withoutId(userId, originalVoteItemId);

        // when & then
        assertThatThrownBy(() -> voteParticipant.changeVoteItem(originalVoteItemId))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining(ErrorCode.VOTE_ITEM_ALREADY_VOTED.getMessage());
    }

    @Test
    @DisplayName("다른 voteItemId로 변경 시 성공")
    void changeVoteItem_success_whenVoteItemIdIsDifferent() {
        // given
        Long userId = 1L;
        Long originalVoteItemId = 10L;
        Long newVoteItemId = 20L;
        VoteParticipant voteParticipant = VoteParticipant.withoutId(userId, originalVoteItemId);

        // when
        voteParticipant.changeVoteItem(newVoteItemId);

        // then
        assertThat(voteParticipant.getVoteItemId()).isEqualTo(newVoteItemId);
    }
}