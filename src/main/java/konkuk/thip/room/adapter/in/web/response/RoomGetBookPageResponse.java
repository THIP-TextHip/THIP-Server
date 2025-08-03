package konkuk.thip.room.adapter.in.web.response;

public record RoomGetBookPageResponse(
        int totalBookPage,
        boolean isOverviewPossible,
        Long roomId
) {
    public static RoomGetBookPageResponse of(int totalBookPage, boolean isOverviewPossible, Long roomId) {
        return new RoomGetBookPageResponse(totalBookPage, isOverviewPossible, roomId);
    }
}
