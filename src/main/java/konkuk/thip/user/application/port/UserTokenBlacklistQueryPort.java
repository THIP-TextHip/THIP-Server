package konkuk.thip.user.application.port;

public interface UserTokenBlacklistQueryPort {
    boolean isTokenBlacklisted(String token);
}
