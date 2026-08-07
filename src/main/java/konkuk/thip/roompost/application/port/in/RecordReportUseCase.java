package konkuk.thip.roompost.application.port.in;

import konkuk.thip.roompost.application.port.in.dto.record.RecordReportResult;

public interface RecordReportUseCase {
    RecordReportResult reportRecord(Long recordId);
}
