package konkuk.thip.roompost.adapter.in.web.response;

import konkuk.thip.roompost.application.port.in.dto.attendancecheck.AttendanceCheckReportResult;

public record AttendanceCheckReportResponse(
        Long attendanceCheckId,
        int reportCount
) {
    public static AttendanceCheckReportResponse of(AttendanceCheckReportResult attendanceCheckReportResult) {
        return new AttendanceCheckReportResponse(attendanceCheckReportResult.attendanceCheckId(), attendanceCheckReportResult.reportCount());
    }
}
