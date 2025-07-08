package konkuk.thip.room.adapter.in.web.response;

public record RoomCreateResponse(Long roomId) {
    public static RoomCreateResponse of(Long roomId) {
        return new RoomCreateResponse(roomId);
    }
}
