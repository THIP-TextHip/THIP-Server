package konkuk.thip.notification.application.port.in;

import konkuk.thip.notification.application.port.in.dto.FcmEnableStateChangeCommand;

public interface FcmEnableStateChangeUseCase {
    void changeEnableState(FcmEnableStateChangeCommand command);
}
