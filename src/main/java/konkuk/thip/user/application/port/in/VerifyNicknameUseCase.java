package konkuk.thip.user.application.port.in;

public interface VerifyNicknameUseCase {

    boolean isNicknameUnique(String nickname);
}
