package konkuk.thip.user.application.port.in;

import konkuk.thip.user.application.port.in.dto.UserBlockCommand;

public interface UserBlockUseCase {

    Boolean changeBlockState(UserBlockCommand blockCommand);
}
