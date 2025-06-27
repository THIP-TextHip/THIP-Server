package konkuk.thip.user.adapter.in.web.response;

public record VerifyNicknameResponse(boolean isVerified) {
    public static VerifyNicknameResponse of(boolean isVerified) {
        return new VerifyNicknameResponse(isVerified);
    }
}
