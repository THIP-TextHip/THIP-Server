package konkuk.thip.user.application.port.in;

public interface UserVerifyNicknameUseCase {

    boolean isNicknameUnique(String nickname);
}
