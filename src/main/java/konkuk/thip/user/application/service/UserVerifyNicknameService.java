package konkuk.thip.user.application.service;

import konkuk.thip.common.annotation.persistence.Unfiltered;
import konkuk.thip.user.application.port.in.UserVerifyNicknameUseCase;
import konkuk.thip.user.application.port.out.UserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserVerifyNicknameService implements UserVerifyNicknameUseCase {

    private final UserQueryPort userQueryPort;

    @Override
    @Transactional(readOnly = true)
    public boolean isNicknameUnique(String nickname) {
        return !userQueryPort.existsByNickname(nickname);
    }
}
