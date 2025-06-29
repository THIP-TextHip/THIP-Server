package konkuk.thip.user.application.port.out;

public interface UserQueryPort {

    boolean existsByNickname(String nickname);
}
