package konkuk.thip.notification.application.service;

import konkuk.thip.notification.application.port.in.FcmDeleteUseCase;
import konkuk.thip.notification.application.port.in.dto.FcmTokenDeleteCommand;
import konkuk.thip.notification.application.port.out.FcmTokenLoadPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmDeleteService implements FcmDeleteUseCase {

    private final FcmTokenLoadPort fcmTokenLoadPort;


    @Override
    @Transactional
    public void deleteToken(FcmTokenDeleteCommand command) {
        fcmTokenLoadPort.deleteByUserIdAndDeviceId(command.userId(), command.deviceId());
    }
}
