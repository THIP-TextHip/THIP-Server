package konkuk.thip.room.application.port.in.dto;

public record AttendanceCheckCreateCommand(
        Long creatorId,
        Long roomId,
        String content
) { }
