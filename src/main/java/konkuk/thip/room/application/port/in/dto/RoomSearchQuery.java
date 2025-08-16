package konkuk.thip.room.application.port.in.dto;

public record RoomSearchQuery(
        String keyword,
        String categoryStr,
        String sortStr,
        boolean isFinalized,
        String cursorStr,
        Long userId
) {
    public static RoomSearchQuery of (String keyword, String categoryStr, String sortStr,
                                      boolean isFinalized, String cursorStr, Long userId) {
        return new RoomSearchQuery(keyword, categoryStr, sortStr, isFinalized, cursorStr, userId);
    }
}
