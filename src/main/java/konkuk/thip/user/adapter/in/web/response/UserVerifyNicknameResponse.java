package konkuk.thip.user.adapter.in.web.response;

public record UserVerifyNicknameResponse(boolean isVerified) {
    public static UserVerifyNicknameResponse of(boolean isVerified) {
        return new UserVerifyNicknameResponse(isVerified);
    }
}
