package konkuk.thip.user.application.port.in;

public interface UserDeleteUseCase {
    void deleteUser(Long userId, String authToken);
}
