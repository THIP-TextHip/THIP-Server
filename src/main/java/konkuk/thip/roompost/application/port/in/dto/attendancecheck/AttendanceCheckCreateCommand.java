package konkuk.thip.roompost.application.port.in.dto.attendancecheck;

public record AttendanceCheckCreateCommand(
        Long creatorId,
        Long roomId,
        String content
) { }
