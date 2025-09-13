package konkuk.thip.roompost.application.service;

import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import konkuk.thip.room.domain.Room;
import konkuk.thip.roompost.application.port.in.RoomPostUpdateUseCase;
import konkuk.thip.roompost.application.port.in.dto.record.RecordUpdateCommand;
import konkuk.thip.roompost.application.port.in.dto.vote.VoteUpdateCommand;
import konkuk.thip.roompost.application.port.out.RecordCommandPort;
import konkuk.thip.roompost.application.port.out.VoteCommandPort;
import konkuk.thip.roompost.domain.Record;
import konkuk.thip.roompost.domain.Vote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomPostUpdateService implements RoomPostUpdateUseCase {

    private final RoomParticipantValidator roomParticipantValidator;
    private final RecordCommandPort recordCommandPort;
    private final VoteCommandPort voteCommandPort;
    private final RoomCommandPort roomCommandPort;

    @Override
    @Transactional
    public Long updateRecord(RecordUpdateCommand command) {
        // 1. 사용자가 방의 참가자인지 검증
        roomParticipantValidator.validateUserIsRoomMember(command.roomId(), command.userId());

        // 1.1 방이 만료되었는지 검증
        validateRoom(command.roomId());

        // 2. Record 조회
        Record record = recordCommandPort.getByIdOrThrow(command.postId());

        // 3. Record 수정
        record.updateRecord(command.userId(), command.roomId(), command.content());

        // 4. Record 업데이트
        recordCommandPort.update(record);
        return command.roomId();
    }

    @Override
    @Transactional
    public Long updateVote(VoteUpdateCommand command) {
        // 1. 사용자가 방의 참가자인지 검증
        roomParticipantValidator.validateUserIsRoomMember(command.roomId(), command.userId());

        // 1.1 방이 만료되었는지 검증
        validateRoom(command.roomId());

        // 2. Vote 조회
        Vote vote = voteCommandPort.getByIdOrThrow(command.postId());

        // 3. Vote 수정
        vote.updateVote(command.userId(), command.roomId(), command.content());

        // 4. Vote 업데이트
        voteCommandPort.updateVote(vote);
        return command.roomId();
    }

    private void validateRoom(Long command) {
        Room room = roomCommandPort.getByIdOrThrow(command);
        room.validateRoomInProgress();
    }
}
