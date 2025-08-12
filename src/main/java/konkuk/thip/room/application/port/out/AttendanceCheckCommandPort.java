package konkuk.thip.room.application.port.out;

import konkuk.thip.room.domain.AttendanceCheck;

public interface AttendanceCheckCommandPort {

    Long save(AttendanceCheck attendanceCheck);
}
