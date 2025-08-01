package konkuk.thip.vote.application.service;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import konkuk.thip.vote.application.port.in.VoteUseCase;
import konkuk.thip.vote.application.port.out.VoteCommandPort;
import konkuk.thip.vote.application.service.dto.VoteCommand;
import konkuk.thip.vote.application.service.dto.VoteResult;
import konkuk.thip.vote.domain.VoteParticipant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoteService implements VoteUseCase {

    private final VoteCommandPort voteCommandPort;

    private final RoomParticipantValidator roomParticipantValidator;

    @Override
    public VoteResult vote(VoteCommand command) {
        log.info("VoteService.vote() - userId: {}, roomId: {}, voteId: {}, voteItemId: {}, type: {}",
                command.userId(), command.roomId(), command.voteId(), command.voteItemId(), command.type());
        // 1. 방 참가자 검증
        roomParticipantValidator.validateUserIsRoomMember(command.roomId(), command.userId());

        if (command.type()) {
            // 2. 투표 아이템에 투표하기
            handleVote(command.userId(), command.voteId(), command.voteItemId());
        } else {
            // 2. 투표 아이템에 투표 취소하기
            handleVoteCancel(command);
        }

        return VoteResult.of(command.voteItemId(), command.roomId(), command.type());
    }

    private void handleVote(Long userId, Long voteId, Long voteItemId) {
        //
        voteCommandPort.findVoteParticipantByUserIdAndVoteId(userId, voteId)
            .ifPresentOrElse(
                    voteParticipant -> { // 이미 투표를 했던 적이 있는 경우
                        voteParticipant.changeVoteItem(voteItemId);
                        voteCommandPort.updateVoteItemFromVoteParticipant(voteParticipant);
                    },
                    () -> {  // 투표를 처음 하는 경우
                        voteCommandPort.saveVoteParticipant(VoteParticipant.withoutId(userId, voteItemId));
                    }
            );
    }

    private void handleVoteCancel(VoteCommand command) {
        // 사용자가 해당 투표 항목에 참여했는지 확인
        voteCommandPort.findVoteParticipantByUserIdAndVoteItemId(command.userId(), command.voteItemId())
            .ifPresentOrElse(
                    // 투표 항목을 변경하여 투표 취소
                    voteCommandPort::deleteVoteParticipant,
                    () -> {
                        // 투표를 하지 않은 경우 예외 처리
                        throw new BusinessException(ErrorCode.VOTE_ITEM_NOT_VOTED_CANNOT_CANCEL);
                    }
            );
    }
}
