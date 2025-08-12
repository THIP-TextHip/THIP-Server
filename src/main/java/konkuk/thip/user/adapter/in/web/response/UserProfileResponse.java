package konkuk.thip.user.adapter.in.web.response;

public record UserProfileResponse(
        String profileImageUrl,
        String nickname,
        String aliasName,
        String aliasColor
) {
    public static UserProfileResponse of(String profileImageUrl, String nickname, String aliasName, String aliasColor) {
        return new UserProfileResponse(profileImageUrl, nickname, aliasName,  aliasColor);
    }
}
