package konkuk.thip.room.application.port.in.dto;

public record RoomVerifyPasswordQuery(
        Long roomId,
        String password
) {
}
