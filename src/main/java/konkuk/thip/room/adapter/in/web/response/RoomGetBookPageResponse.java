package konkuk.thip.room.adapter.in.web.response;

public record RoomGetBookPageResponse(
        int totalBookPage,
        boolean isOverviewPossible
) {
    public static RoomGetBookPageResponse of(int totalBookPage, boolean isOverviewPossible) {
        return new RoomGetBookPageResponse(totalBookPage, isOverviewPossible);
    }
}
