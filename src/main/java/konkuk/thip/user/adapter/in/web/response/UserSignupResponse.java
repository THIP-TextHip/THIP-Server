package konkuk.thip.user.adapter.in.web.response;

public record UserSignupResponse(Long userId) {
    public static UserSignupResponse of(Long userId) {
        return new UserSignupResponse(userId);
    }
}