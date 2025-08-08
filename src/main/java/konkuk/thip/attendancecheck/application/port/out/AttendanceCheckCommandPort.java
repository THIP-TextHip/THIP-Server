package konkuk.thip.attendancecheck.application.port.out;

import konkuk.thip.attendancecheck.domain.AttendanceCheck;

public interface AttendanceCheckCommandPort {

    Long save(AttendanceCheck attendanceCheck);
}
