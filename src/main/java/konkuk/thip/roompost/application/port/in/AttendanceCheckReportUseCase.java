package konkuk.thip.roompost.application.port.in;

import konkuk.thip.roompost.application.port.in.dto.attendancecheck.AttendanceCheckReportResult;

public interface AttendanceCheckReportUseCase {
    AttendanceCheckReportResult reportAttendanceCheck(Long attendanceCheckId);
}
