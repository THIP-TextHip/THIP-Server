package konkuk.thip.user.application.port.in;

import konkuk.thip.user.application.port.in.dto.UserSignupCommand;
import konkuk.thip.user.application.port.in.dto.UserSignupResult;

public interface UserSignupUseCase {

    UserSignupResult signup(UserSignupCommand command);
}
