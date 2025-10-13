package konkuk.thip.roompost.application.port.in;

import konkuk.thip.roompost.application.port.in.dto.record.RecordAiUsageResult;

public interface RecordAiUsageUseCase {
    RecordAiUsageResult getUserAiUsage(Long userId, Long roomId);
}
