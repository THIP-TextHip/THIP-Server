package konkuk.thip.roompost.application.service;

import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import konkuk.thip.roompost.application.port.in.RecordAiUsageUseCase;
import konkuk.thip.roompost.application.port.in.dto.record.RecordAiUsageResult;
import konkuk.thip.roompost.application.port.out.RecordQueryPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecordAiUsageService implements RecordAiUsageUseCase {

    private final RecordQueryPort recordQueryPort;
    private final RoomParticipantValidator roomParticipantValidator;
    private final UserCommandPort userCommandPort;

    @Override
    public RecordAiUsageResult getUserAiUsage(Long userId, Long roomId) {
        roomParticipantValidator.validateUserIsRoomMember(roomId, userId);

        Integer recordCount = recordQueryPort.countAllByRoomIdAndUserId(roomId, userId);
        Integer recordReviewCount = userCommandPort.findById(userId).getRecordReviewCount();

        return new RecordAiUsageResult(recordReviewCount, recordCount);
    }
}
