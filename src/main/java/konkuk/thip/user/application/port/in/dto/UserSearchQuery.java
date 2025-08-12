package konkuk.thip.user.application.port.in.dto;

public record UserSearchQuery(
        String keyword,
        Long userId,
        Integer size
) {
    public static UserSearchQuery of(String keyword, Long userId, Integer size) {
        return new UserSearchQuery(keyword, userId, size);
    }
}
