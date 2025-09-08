package konkuk.thip.user.application.port;

public interface UserTokenBlacklistCommandPort {
    void addTokenToBlacklist(String token);
}
