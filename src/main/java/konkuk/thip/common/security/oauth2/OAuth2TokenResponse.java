package konkuk.thip.common.security.oauth2;

public record OAuth2TokenResponse(
        String token,
        boolean isNewUser
) {
    public static OAuth2TokenResponse of(String token, boolean isNewUser) {
        return new OAuth2TokenResponse(token, isNewUser);
    }
}
