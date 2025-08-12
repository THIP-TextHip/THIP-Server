package konkuk.thip.room.application.port.in.dto;

public record RoomJoinResult(
        Long roomId,
        String type
) {
    public static RoomJoinResult of(Long roomId, String type) {
        return new RoomJoinResult(roomId, type);
    }
}
