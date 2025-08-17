package konkuk.thip.common.security.oauth2.auth.dto;

public record AuthTokenResponse(
        String token,
        boolean isNewUser,
        String tokenType
) {
    public static AuthTokenResponse of(String token, boolean isNewUser, String tokenType) {
        return new AuthTokenResponse(token, isNewUser, tokenType);
    }
}
