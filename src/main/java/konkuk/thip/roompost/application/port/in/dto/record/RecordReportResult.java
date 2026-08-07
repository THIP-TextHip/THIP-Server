package konkuk.thip.roompost.application.port.in.dto.record;

public record RecordReportResult(
        Long recordId,
        int reportCount
) {
    public static RecordReportResult of(Long recordId, int reportCount) {
        return new RecordReportResult(recordId, reportCount);
    }
}
