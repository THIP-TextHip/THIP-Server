package konkuk.thip.roompost.application.port.in;

import konkuk.thip.roompost.application.port.in.dto.attendancecheck.AttendanceCheckCreateCommand;
import konkuk.thip.roompost.application.port.in.dto.attendancecheck.AttendanceCheckCreateResult;

public interface AttendanceCheckCreateUseCase {

    AttendanceCheckCreateResult create(AttendanceCheckCreateCommand command);
}
