package konkuk.thip.vote.application.service;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import konkuk.thip.vote.application.port.in.VoteUseCase;
import konkuk.thip.vote.application.port.out.VoteCommandPort;
import konkuk.thip.vote.application.service.dto.VoteCommand;
import konkuk.thip.vote.application.service.dto.VoteResult;
import konkuk.thip.vote.domain.VoteItem;
import konkuk.thip.vote.domain.VoteParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoteService implements VoteUseCase {

    private final VoteCommandPort voteCommandPort;
    private final RoomParticipantValidator roomParticipantValidator;

    @Override
    public VoteResult vote(VoteCommand command) {
        // 1. 방 참가자 검증
        roomParticipantValidator.validateUserIsRoomMember(command.roomId(), command.userId());

        if (command.type()) {
            // 투표하기
            voteOrUpdate(command.userId(), command.voteId(), command.voteItemId());
        } else {
            // 투표 취소
            cancelVote(command.userId(), command.voteItemId());
        }

        return VoteResult.of(command.voteItemId(), command.roomId(), command.type());
    }

    private void voteOrUpdate(Long userId, Long voteId, Long voteItemId) {
        // 사용자가 해당 투표 항목에 참여했는지 확인
        voteCommandPort.findVoteParticipantByUserIdAndVoteId(userId, voteId)
                .ifPresentOrElse(
                        // 투표를 이미 한 경우
                        participant -> updateVote(participant, voteItemId),
                        // 투표를 처음 하는 경우
                        () -> createVote(userId, voteItemId)
                );
    }

    private void cancelVote(Long userId, Long voteItemId) {
        // 사용자가 해당 투표 항목에 참여했는지 확인
        voteCommandPort.findVoteParticipantByUserIdAndVoteItemId(userId, voteItemId)
                .ifPresentOrElse(
                        // 투표 취소
                        participant -> removeVote(participant, voteItemId),
                        () -> {
                            // 투표를 하지 않은 경우 예외 처리
                            throw new BusinessException(ErrorCode.VOTE_ITEM_NOT_VOTED_CANNOT_CANCEL);
                        }
                );
    }

    private void updateVote(VoteParticipant participant, Long newVoteItemId) {
        participant.changeVoteItem(newVoteItemId);
        voteCommandPort.updateVoteParticipant(participant);
    }

    private void createVote(Long userId, Long voteItemId) {
        modifyVoteCount(voteItemId, true);
        voteCommandPort.saveVoteParticipant(VoteParticipant.withoutId(userId, voteItemId));
    }

    private void removeVote(VoteParticipant participant, Long voteItemId) {
        modifyVoteCount(voteItemId, false);
        voteCommandPort.deleteVoteParticipant(participant);
    }

    private void modifyVoteCount(Long voteItemId, boolean isIncrease) {
        VoteItem voteItem = voteCommandPort.getVoteItemByIdOrThrow(voteItemId);
        if (isIncrease) {
            voteItem.increaseCount();
        } else {
            voteItem.decreaseCount();
        }
        voteCommandPort.updateVoteItem(voteItem);
    }
}