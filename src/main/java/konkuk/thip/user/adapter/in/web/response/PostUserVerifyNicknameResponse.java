package konkuk.thip.user.adapter.in.web.response;

public record PostUserVerifyNicknameResponse(boolean isVerified) {
    public static PostUserVerifyNicknameResponse of(boolean isVerified) {
        return new PostUserVerifyNicknameResponse(isVerified);
    }
}
