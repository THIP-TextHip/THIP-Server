package konkuk.thip.roompost.application.port.in.dto.attendancecheck;

public record AttendanceCheckReportResult(
        Long attendanceCheckId,
        int reportCount
) {
    public static AttendanceCheckReportResult of(Long attendanceCheckId, int reportCount) {
        return new AttendanceCheckReportResult(attendanceCheckId, reportCount);
    }
}
