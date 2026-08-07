package konkuk.thip.roompost.application.service;

import konkuk.thip.roompost.application.port.in.AttendanceCheckReportUseCase;
import konkuk.thip.roompost.application.port.in.dto.attendancecheck.AttendanceCheckReportResult;
import konkuk.thip.roompost.application.port.out.AttendanceCheckCommandPort;
import konkuk.thip.roompost.domain.AttendanceCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceCheckReportService implements AttendanceCheckReportUseCase {

    private final AttendanceCheckCommandPort attendanceCheckCommandPort;

    @Override
    @Transactional
    public AttendanceCheckReportResult reportAttendanceCheck(Long attendanceCheckId) {
        AttendanceCheck attendanceCheck = attendanceCheckCommandPort.getByIdOrThrow(attendanceCheckId);
        attendanceCheck.increaseReportCount();
        attendanceCheckCommandPort.update(attendanceCheck);

        return AttendanceCheckReportResult.of(attendanceCheck.getId(), attendanceCheck.getReportCount());
    }
}
