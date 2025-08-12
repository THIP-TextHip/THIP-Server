package konkuk.thip.user.application.port.in.dto;

public record UserSearchQuery(
        String keyword,
        Long userId,
        Integer size,
        boolean isFinalized
) {
    public static UserSearchQuery of(String keyword, Long userId, Integer size, boolean isFinalized) {
        return new UserSearchQuery(keyword, userId, size, isFinalized);
    }
}
