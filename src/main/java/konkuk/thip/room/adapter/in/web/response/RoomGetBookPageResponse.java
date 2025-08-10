package konkuk.thip.room.adapter.in.web.response;

public record RoomGetBookPageResponse(
        int totalBookPage,
        int recentBookPage,
        boolean isOverviewPossible,
        Long roomId
) {
    public static RoomGetBookPageResponse of(int totalBookPage, int recentBookPage, boolean isOverviewPossible, Long roomId) {
        return new RoomGetBookPageResponse(totalBookPage, recentBookPage, isOverviewPossible, roomId);
    }
}
