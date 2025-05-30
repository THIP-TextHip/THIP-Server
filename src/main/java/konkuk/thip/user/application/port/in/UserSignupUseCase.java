package konkuk.thip.user.application.port.in;

import konkuk.thip.user.application.port.in.dto.UserSignupCommand;

public interface UserSignupUseCase {

    Long signup(UserSignupCommand command);
}
