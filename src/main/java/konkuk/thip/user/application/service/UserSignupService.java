package konkuk.thip.user.application.service;

import konkuk.thip.user.application.port.in.UserSignupUseCase;
import konkuk.thip.user.application.port.in.dto.UserSignupCommand;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSignupService implements UserSignupUseCase {

    private final UserCommandPort userCommandPort;

    @Transactional
    @Override
    public Long signup(UserSignupCommand command) {
        // User 엔티티 생성
        User user = User.withoutId(command.getName(), command.getEmail(), command.getPassword());

        // 비즈니스 로직,,,,

        // DB i/o
        return userCommandPort.save(user);
    }
}
