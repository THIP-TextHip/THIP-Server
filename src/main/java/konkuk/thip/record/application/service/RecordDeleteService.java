package konkuk.thip.record.application.service;

import jakarta.transaction.Transactional;
import konkuk.thip.record.application.port.in.RecordDeleteUseCase;
import konkuk.thip.record.application.port.in.dto.RecordDeleteCommand;
import konkuk.thip.record.application.port.out.RecordCommandPort;
import konkuk.thip.record.domain.Record;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecordDeleteService implements RecordDeleteUseCase {

    private final RecordCommandPort recordCommandPort;
    private final RoomParticipantValidator roomParticipantValidator;

    @Override
    @Transactional
    public Long deleteRecord(RecordDeleteCommand command) {

        // 1. 방 참여자 검증
        roomParticipantValidator.validateUserIsRoomMember(command.roomId(), command.userId());

        // 2. 기록 조회 및 검증
        Record record = recordCommandPort.getByIdOrThrow(command.recordId());
        // 2-1. 기록 삭제 권한 검증
        record.validateRoomId(command.roomId());
        record.validateDeletable(command.userId());

        // 3. 기록 삭제
        recordCommandPort.delete(record);

        return command.roomId();
    }
}
