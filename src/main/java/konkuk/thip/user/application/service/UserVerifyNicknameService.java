package konkuk.thip.user.application.service;

import konkuk.thip.user.application.port.in.UserVerifyNicknameUseCase;
import konkuk.thip.user.application.port.out.UserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserVerifyNicknameService implements UserVerifyNicknameUseCase {

    private final UserQueryPort userQueryPort;

    @Override
    public boolean isNicknameUnique(String nickname) {
        return !userQueryPort.existsByNickname(nickname);
    }
}
