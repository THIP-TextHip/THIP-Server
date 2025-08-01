package konkuk.thip.vote.application.service;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import konkuk.thip.vote.application.port.out.VoteCommandPort;
import konkuk.thip.vote.application.port.in.dto.VoteCommand;
import konkuk.thip.vote.application.port.in.dto.VoteResult;
import konkuk.thip.vote.domain.VoteItem;
import konkuk.thip.vote.domain.VoteParticipant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class VoteServiceTest {

    private VoteCommandPort voteCommandPort;
    private RoomParticipantValidator roomParticipantValidator;
    private VoteService voteService;

    @BeforeEach
    void setUp() {
        voteCommandPort = mock(VoteCommandPort.class);
        roomParticipantValidator = mock(RoomParticipantValidator.class);
        voteService = new VoteService(voteCommandPort, roomParticipantValidator);
    }

    @Test
    @DisplayName("처음 투표하는 경우 - 새로운 VoteParticipant 저장")
    void vote_firstTimeVote_success() {
        // given
        VoteItem voteItem = mock(VoteItem.class);
        VoteCommand command = new VoteCommand(1L, 1L, 1L, 100L, true);


        // when
        when(voteCommandPort.findVoteParticipantByUserIdAndVoteId(1L, 1L))
                .thenReturn(Optional.empty());
        when(voteCommandPort.getVoteItemByIdOrThrow(100L)).thenReturn(voteItem);
        VoteResult result = voteService.vote(command);

        // then
        assertThat(result.voteItemId()).isEqualTo(100L);
        verify(roomParticipantValidator).validateUserIsRoomMember(1L, 1L);
        verify(voteItem).increaseCount();
        verify(voteCommandPort).updateVoteItem(voteItem);
        verify(voteCommandPort).saveVoteParticipant(any(VoteParticipant.class));
    }

    @Test
    @DisplayName("이미 투표한 경우 - 다른 voteItemId로 변경 성공")
    void vote_alreadyVoted_changeVoteItem_success() {
        // given
        VoteCommand command = new VoteCommand(1L, 1L, 1L, 200L, true);
        VoteParticipant existing = VoteParticipant.withoutId(1L, 100L);

        // when
        when(voteCommandPort.findVoteParticipantByUserIdAndVoteId(1L, 1L))
                .thenReturn(Optional.of(existing));
        when(voteCommandPort.getVoteItemByIdOrThrow(200L)).thenReturn(mock(VoteItem.class));
        VoteResult result = voteService.vote(command);

        // then
        assertThat(result.voteItemId()).isEqualTo(200L);
        verify(voteCommandPort).updateVoteParticipant(existing);
    }

    @Test
    @DisplayName("이미 투표한 경우 - 같은 voteItemId로 변경 시 예외 발생")
    void vote_alreadyVoted_sameVoteItem_throwsException() {
        // given
        VoteCommand command = new VoteCommand(1L, 1L, 1L, 100L, true);
        VoteParticipant existing = VoteParticipant.withoutId(1L, 100L);
        when(voteCommandPort.findVoteParticipantByUserIdAndVoteId(1L, 1L))
                .thenReturn(Optional.of(existing));

        // then
        assertThatThrownBy(() -> voteService.vote(command))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining(ErrorCode.VOTE_ITEM_ALREADY_VOTED.getMessage());
    }

    @Test
    @DisplayName("투표 취소 - 해당 voteItem에 투표한 적이 있으면 삭제")
    void vote_cancelVote_success() {
        // given
        VoteItem voteItem = mock(VoteItem.class);

        VoteCommand command = new VoteCommand(1L, 1L, 1L, 300L, false);
        VoteParticipant existing = VoteParticipant.withoutId(1L, 300L);

        // when
        when(voteCommandPort.getVoteItemByIdOrThrow(300L)).thenReturn(voteItem);
        when(voteCommandPort.findVoteParticipantByUserIdAndVoteItemId(1L, 300L))
                .thenReturn(Optional.of(existing));
        VoteResult result = voteService.vote(command);

        // then
        assertThat(result.type()).isFalse();
        verify(voteItem).decreaseCount();
        verify(voteCommandPort).updateVoteItem(voteItem);
        verify(voteCommandPort).deleteVoteParticipant(existing);
    }

    @Test
    @DisplayName("투표 취소 - 투표한 적이 없는 경우 예외 발생")
    void vote_cancelVote_withoutExistingVote_throwsException() {
        // given
        VoteCommand command = new VoteCommand(1L, 1L, 1L, 300L, false);
        when(voteCommandPort.findVoteParticipantByUserIdAndVoteItemId(1L, 300L))
                .thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> voteService.vote(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.VOTE_ITEM_NOT_VOTED_CANNOT_CANCEL.getMessage());
    }
}