package konkuk.thip.notification.application.port.in.fcm;

import konkuk.thip.notification.application.port.in.dto.FcmEnableStateChangeCommand;

public interface FcmEnableStateChangeUseCase {
    boolean changeEnableState(FcmEnableStateChangeCommand command);
}
