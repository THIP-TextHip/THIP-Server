package konkuk.thip.user.application.port.in;

import konkuk.thip.user.application.port.in.dto.UserReadResult;

public interface UserReadUseCase {

    UserReadResult readUser(Long id);
}
