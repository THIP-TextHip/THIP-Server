package konkuk.thip.room.application.port.in;

import konkuk.thip.room.application.port.in.dto.AttendanceCheckCreateCommand;
import konkuk.thip.room.application.port.in.dto.AttendanceCheckCreateResult;

public interface AttendanceCheckCreateUseCase {

    AttendanceCheckCreateResult create(AttendanceCheckCreateCommand command);
}
