package konkuk.thip.room.application.port.in.dto;

public record RoomJoinCommand(
        Long userId,
        Long roomId,
        String type
) {
}
