package konkuk.thip.user.application.port.in.dto;

public record UserSignupResult(
        Long userId,
        String accessToken
) {
    public static UserSignupResult of(Long userId, String accessToken) {
        return new UserSignupResult(userId, accessToken);
    }
}
