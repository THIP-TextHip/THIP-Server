package konkuk.thip.common.security.oauth2;

public record LoginUser(
    String oauth2Id,
    Long userId,
    String email,
    boolean isNewUser
) {
    public static LoginUser createNewUser(String oauth2Id, String email) {
        return new LoginUser(oauth2Id, null, email,true);
    }

    public static LoginUser createExistingUser(String oauth2Id, Long userId, String email) {
        return new LoginUser(oauth2Id, userId, email, false);
    }
}
