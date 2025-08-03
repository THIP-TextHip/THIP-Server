package konkuk.thip.room.adapter.in.web.response;

public record RoomRecruitCloseResponse(
        Long roomId
) {
    public static RoomRecruitCloseResponse of(Long roomId) {
        return new RoomRecruitCloseResponse(roomId);
    }
}
