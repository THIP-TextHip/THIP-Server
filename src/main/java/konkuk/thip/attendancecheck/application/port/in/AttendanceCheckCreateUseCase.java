package konkuk.thip.attendancecheck.application.port.in;

import konkuk.thip.attendancecheck.application.port.in.dto.AttendanceCheckCreateCommand;

public interface AttendanceCheckCreateUseCase {

    Long create(AttendanceCheckCreateCommand command);
}
