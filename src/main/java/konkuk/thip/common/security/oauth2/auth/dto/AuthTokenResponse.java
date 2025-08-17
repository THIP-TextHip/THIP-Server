package konkuk.thip.common.security.oauth2.auth.dto;

public record AuthTokenResponse(
        String token,
        boolean isNewUser
) {
    public static AuthTokenResponse of(String token, boolean isNewUser) {
        return new AuthTokenResponse(token, isNewUser);
    }
}
