package konkuk.thip.room.application.port.in.dto;

public record RoomSearchQuery(
        String keyword,
        String categoryStr,
        String sortStr,
        boolean isFinalized,
        String cursorStr,
        Long userId,
        boolean isAllCategory
) {
    public static RoomSearchQuery of (String keyword, String categoryStr, String sortStr,
                                      boolean isFinalized, String cursorStr, Long userId, boolean isAllCategory) {
        return new RoomSearchQuery(keyword, categoryStr, sortStr, isFinalized, cursorStr, userId, isAllCategory);
    }
}
