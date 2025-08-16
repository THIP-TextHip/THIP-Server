package konkuk.thip.roompost.application.port.out;

import konkuk.thip.roompost.domain.AttendanceCheck;

public interface AttendanceCheckCommandPort {

    Long save(AttendanceCheck attendanceCheck);
}
