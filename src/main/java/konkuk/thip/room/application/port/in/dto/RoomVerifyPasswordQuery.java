package konkuk.thip.room.application.port.in.dto;

public record RoomVerifyPasswordQuery(
        Long userId,
        Long roomId,
        String password
) {
}
