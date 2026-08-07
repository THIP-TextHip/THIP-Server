package konkuk.thip.roompost.application.service;

import konkuk.thip.roompost.application.port.in.RecordReportUseCase;
import konkuk.thip.roompost.application.port.in.dto.record.RecordReportResult;
import konkuk.thip.roompost.application.port.out.RecordCommandPort;
import konkuk.thip.roompost.domain.Record;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecordReportService implements RecordReportUseCase {

    private final RecordCommandPort recordCommandPort;

    @Override
    @Transactional
    public RecordReportResult reportRecord(Long recordId) {
        Record record = recordCommandPort.getByIdOrThrow(recordId);
        record.increaseReportCount();
        recordCommandPort.update(record);

        return RecordReportResult.of(record.getId(), record.getReportCount());
    }
}
