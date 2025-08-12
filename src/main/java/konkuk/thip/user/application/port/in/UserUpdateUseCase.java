package konkuk.thip.user.application.port.in;

import konkuk.thip.user.application.port.in.dto.UserUpdateCommand;

public interface UserUpdateUseCase {
    void updateUser(UserUpdateCommand command);
}
