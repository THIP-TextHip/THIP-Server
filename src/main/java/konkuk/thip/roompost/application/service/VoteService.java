package konkuk.thip.roompost.application.service;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import konkuk.thip.room.domain.Room;
import konkuk.thip.roompost.application.port.in.VoteUseCase;
import konkuk.thip.roompost.application.port.in.dto.vote.VoteCommand;
import konkuk.thip.roompost.application.port.in.dto.vote.VoteResult;
import konkuk.thip.roompost.application.port.out.VoteCommandPort;
import konkuk.thip.roompost.application.port.out.VoteQueryPort;
import konkuk.thip.roompost.application.port.out.dto.VoteItemQueryDto;
import konkuk.thip.roompost.domain.VoteItem;
import konkuk.thip.roompost.domain.VoteParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class VoteService implements VoteUseCase {

    private final VoteCommandPort voteCommandPort;
    private final VoteQueryPort voteQueryPort;
    private final RoomParticipantValidator roomParticipantValidator;
    private final RoomCommandPort roomCommandPort;

    @Override
    @Transactional
    public VoteResult vote(VoteCommand command) {
        // 1. 방 참가자 검증
        roomParticipantValidator.validateUserIsRoomMember(command.roomId(), command.userId());

        // 1.1 방 존재 여부 및 만료 검증
        Room room = roomCommandPort.getByIdOrThrow(command.roomId());
        room.validateRoomInProgress();

        if (command.type()) {
            // 투표하기
            voteOrUpdate(command.userId(), command.voteId(), command.voteItemId());
        } else {
            // 투표 취소
            cancelVote(command.userId(), command.voteItemId());
        }

        // 2. 투표 결과 반환
        List<VoteItemQueryDto> voteItems = voteQueryPort.findVoteItemsByVoteId(command.voteId(), command.userId());

        var voteItemDtos = IntStream.range(0, voteItems.size())
                .mapToObj(i -> VoteResult.VoteItemDto.of(
                        voteItems.get(i).voteItemId(),
                        voteItems.get(i).itemName(),
                        voteItems.get(i).voteCount(),
                        voteItems.get(i).isVoted()
                ))
                .toList();

        return VoteResult.of(command.voteId(), command.roomId(), voteItemDtos);
    }

    private void voteOrUpdate(Long userId, Long voteId, Long voteItemId) {
        // 사용자가 해당 투표에 참여했는지 확인
        voteCommandPort.findVoteParticipantByUserIdAndVoteId(userId, voteId)
                .ifPresentOrElse(
                        // 투표를 이미 한 경우
                        participant -> updateVote(participant, voteItemId, participant.getVoteItemId()),
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

    private void updateVote(VoteParticipant participant, Long newVoteItemId, Long oldVoteItemId) {
        participant.changeVoteItem(newVoteItemId);
        // 투표 항목 변경 시, 기존 투표 항목의 카운트 감소 및 새로운 투표 항목의 카운트 증가
        updateVoteCount(oldVoteItemId, false);
        updateVoteCount(newVoteItemId, true);

        voteCommandPort.updateVoteParticipant(participant);
    }

    private void createVote(Long userId, Long voteItemId) {
        updateVoteCount(voteItemId, true);
        voteCommandPort.saveVoteParticipant(VoteParticipant.withoutId(userId, voteItemId));
    }

    private void removeVote(VoteParticipant participant, Long voteItemId) {
        updateVoteCount(voteItemId, false);
        voteCommandPort.deleteVoteParticipant(participant);
    }

    private void updateVoteCount(Long voteItemId, boolean isIncrease) {
        VoteItem voteItem = voteCommandPort.getVoteItemByIdOrThrow(voteItemId);
        if (isIncrease) {
            voteItem.increaseCount();
        } else {
            voteItem.decreaseCount();
        }
        voteCommandPort.updateVoteItem(voteItem);
    }
}