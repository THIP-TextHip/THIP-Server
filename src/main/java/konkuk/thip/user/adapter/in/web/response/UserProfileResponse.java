package konkuk.thip.user.adapter.in.web.response;

public record UserProfileResponse(
        String profileImageUrl,
        String nickname,
        String aliasName
) {
    public static UserProfileResponse of(String profileImageUrl, String nickname, String aliasName) {
        return new UserProfileResponse(profileImageUrl, nickname, aliasName);
    }
}
