package konkuk.thip.roompost.adapter.in.web.response;

import konkuk.thip.roompost.application.port.in.dto.attendancecheck.AttendanceCheckCreateResult;

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
