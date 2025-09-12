package konkuk.thip.roompost.application.service;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import konkuk.thip.roompost.application.port.in.dto.vote.VoteCommand;
import konkuk.thip.roompost.application.port.in.dto.vote.VoteResult;
import konkuk.thip.roompost.application.port.out.VoteCommandPort;
import konkuk.thip.roompost.application.port.out.VoteQueryPort;
import konkuk.thip.roompost.application.port.out.dto.VoteItemQueryDto;
import konkuk.thip.roompost.domain.VoteItem;
import konkuk.thip.roompost.domain.VoteParticipant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VoteServiceTest {

    private VoteCommandPort voteCommandPort;
    private VoteQueryPort voteQueryPort;
    private RoomParticipantValidator roomParticipantValidator;
    private VoteService voteService;
    private RoomCommandPort roomCommandPort;

    @BeforeEach
    void setUp() {
        voteCommandPort = mock(VoteCommandPort.class);
        voteQueryPort = mock(VoteQueryPort.class);
        roomParticipantValidator = mock(RoomParticipantValidator.class);
        roomCommandPort = mock(RoomCommandPort.class);
        voteService = new VoteService(voteCommandPort, voteQueryPort, roomParticipantValidator, roomCommandPort);
    }

    private void mockVoteQueryResult(Long voteId, Long voteItemId, String itemName, boolean isVoted, int count) {
        when(voteQueryPort.findVoteItemsByVoteId(any(), any()))
                .thenReturn(List.of(
                        new VoteItemQueryDto(voteId, voteItemId, itemName, count, isVoted)
                ));
    }

    @Test
    @DisplayName("처음 투표하는 경우 - 새로운 VoteParticipant 저장")
    void vote_firstTimeVote_success() {
        // given
        VoteItem voteItem = mock(VoteItem.class);
        VoteCommand command = new VoteCommand(1L, 1L, 1L, 100L, true);

        when(voteCommandPort.findVoteParticipantByUserIdAndVoteId(1L, 1L))
                .thenReturn(Optional.empty());
        when(voteCommandPort.getVoteItemByIdOrThrow(100L)).thenReturn(voteItem);
        mockVoteQueryResult(1L, 100L, "item1", true, 1);

        // when
        VoteResult result = voteService.vote(command);

        // then
        assertThat(result.voteItems()).hasSize(1);
        assertThat(result.voteItems().get(0).voteItemId()).isEqualTo(100L);
        assertThat(result.voteItems().get(0).isVoted()).isTrue();

        verify(roomParticipantValidator).validateUserIsRoomMember(1L, 1L);
        verify(voteItem).increaseCount();
        verify(voteCommandPort).updateVoteItem(voteItem);
        verify(voteCommandPort).saveVoteParticipant(any(VoteParticipant.class));
    }

    @Test
    @DisplayName("이미 투표한 경우 - 다른 voteItemId로 변경 성공")
    void vote_alreadyVoted_changeVoteItem_success() {
        // given
        VoteItem newVoteItem = mock(VoteItem.class);
        VoteItem oldVoteItem = mock(VoteItem.class);
        VoteCommand command = new VoteCommand(1L, 1L, 1L, 200L, true);
        VoteParticipant existing = VoteParticipant.withoutId(1L, 100L);

        when(voteCommandPort.findVoteParticipantByUserIdAndVoteId(1L, 1L))
                .thenReturn(Optional.of(existing));
        when(voteCommandPort.getVoteItemByIdOrThrow(200L)).thenReturn(newVoteItem);
        when(voteCommandPort.getVoteItemByIdOrThrow(100L)).thenReturn(oldVoteItem);
        mockVoteQueryResult(1L, 200L, "item2", true, 5);

        // when
        VoteResult result = voteService.vote(command);

        // then
        assertThat(result.voteItems().get(0).voteItemId()).isEqualTo(200L);
        assertThat(result.voteItems().get(0).isVoted()).isTrue();

        verify(voteCommandPort).updateVoteParticipant(existing);
        verify(newVoteItem).increaseCount();
        verify(voteCommandPort).updateVoteItem(newVoteItem);
        verify(oldVoteItem).decreaseCount();
        verify(voteCommandPort).updateVoteItem(oldVoteItem);
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

        when(voteCommandPort.getVoteItemByIdOrThrow(300L)).thenReturn(voteItem);
        when(voteCommandPort.findVoteParticipantByUserIdAndVoteItemId(1L, 300L))
                .thenReturn(Optional.of(existing));
        mockVoteQueryResult(1L, 300L, "item3", false, 0);

        // when
        VoteResult result = voteService.vote(command);

        // then
        assertThat(result.voteItems().get(0).isVoted()).isFalse();
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
