package konkuk.thip.common.security.oauth2;

public record LoginUser(
    String oauth2Id,
    Long userId,
    boolean isNewUser
) {
    public static LoginUser createNewUser(String oauth2Id) {
        return new LoginUser(oauth2Id, null, true);
    }

    public static LoginUser createExistingUser(String oauth2Id, Long userId) {
        return new LoginUser(oauth2Id, userId, false);
    }
}
