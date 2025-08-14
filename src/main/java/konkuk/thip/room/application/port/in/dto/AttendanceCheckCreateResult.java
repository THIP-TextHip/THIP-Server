package konkuk.thip.room.application.port.in.dto;

import lombok.Builder;

@Builder
public record AttendanceCheckCreateResult(
        Long attendanceCheckId,
        Long roomId,
        int todayWriteCountOfUser
) { }
