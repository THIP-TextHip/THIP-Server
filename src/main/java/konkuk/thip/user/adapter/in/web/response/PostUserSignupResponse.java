package konkuk.thip.user.adapter.in.web.response;

public record PostUserSignupResponse(Long userId) {
    public static PostUserSignupResponse of(Long userId) {
        return new PostUserSignupResponse(userId);
    }
}