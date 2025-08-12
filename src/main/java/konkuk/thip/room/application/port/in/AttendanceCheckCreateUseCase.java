package konkuk.thip.room.application.port.in;

import konkuk.thip.room.application.port.in.dto.AttendanceCheckCreateCommand;

public interface AttendanceCheckCreateUseCase {

    Long create(AttendanceCheckCreateCommand command);
}
