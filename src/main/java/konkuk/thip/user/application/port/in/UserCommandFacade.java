package konkuk.thip.user.application.port.in;

import konkuk.thip.user.application.port.in.dto.UserSignupCommand;
import konkuk.thip.user.application.port.in.dto.UserUpdateCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCommandFacade {

    /**
     * 여기에 command 관련 use case(= incoming port = interface) 전부 주입
     */
    private final UserSignupUseCase userSignupUseCase;
    private final UserUpdateUseCase userUpdateUseCase;

    public Long signup(UserSignupCommand command) {
        return userSignupUseCase.signup(command);
    }

    public void updateUser(UserUpdateCommand command) {
        userUpdateUseCase.update(command);
    }
}
