package konkuk.thip.room.adapter.in.web.response;

import konkuk.thip.room.application.port.in.dto.AttendanceCheckCreateResult;

public record AttendanceCheckCreateResponse(
        Long attendanceCheckId,
        Long roomId,
        boolean isFirstWrite
) {
    public static AttendanceCheckCreateResponse of(AttendanceCheckCreateResult result) {
        boolean isFirstWrite = false;
        if (result.todayWriteCountOfUser() == 1) isFirstWrite = true;

        return new AttendanceCheckCreateResponse(
                result.attendanceCheckId(),
                result.roomId(),
                isFirstWrite
        );
    }
}
