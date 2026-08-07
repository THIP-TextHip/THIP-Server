package konkuk.thip.roompost.adapter.in.web.response;

import konkuk.thip.roompost.application.port.in.dto.record.RecordReportResult;

public record RecordReportResponse(
        Long recordId,
        int reportCount
) {
    public static RecordReportResponse of(RecordReportResult recordReportResult) {
        return new RecordReportResponse(recordReportResult.recordId(), recordReportResult.reportCount());
    }
}
