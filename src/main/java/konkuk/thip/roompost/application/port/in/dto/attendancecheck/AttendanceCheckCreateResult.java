package konkuk.thip.roompost.application.port.in.dto.attendancecheck;

import lombok.Builder;

@Builder
public record AttendanceCheckCreateResult(
        Long attendanceCheckId,
        Long roomId,
        int todayWriteCountOfUser
) { }
