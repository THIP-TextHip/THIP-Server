package konkuk.thip.notification.application.service.fcm;

import konkuk.thip.notification.application.port.in.fcm.FcmRegisterUseCase;
import konkuk.thip.notification.application.port.in.dto.FcmTokenRegisterCommand;
import konkuk.thip.notification.application.port.out.FcmTokenPersistencePort;
import konkuk.thip.notification.domain.FcmToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FcmRegisterService implements FcmRegisterUseCase {

    private final FcmTokenPersistencePort fcmTokenPersistencePort;

    @Override
    @Transactional
    public void registerToken(FcmTokenRegisterCommand command) {
        // 같은 디바이스로 등록된 토큰이 있는지 확인
        fcmTokenPersistencePort.findByDeviceId(command.deviceId()).ifPresentOrElse(
                existingToken -> {
                    // 있으면 새로운 계정으로 교체 또는 기존 계정의 토큰 갱신
                    existingToken.updateToken(
                            command.fcmToken(),
                            command.platformType(),
                            LocalDate.now(),
                            command.userId()
                    );
                    fcmTokenPersistencePort.update(existingToken);
                },
                () -> {
                    // 없으면 새로 등록
                    fcmTokenPersistencePort.save(FcmToken.withoutId(
                            command.fcmToken(),
                            command.deviceId(),
                            command.platformType(),
                            LocalDate.now(),
                            true,
                            command.userId()
                    ));
                }
        );
    }
}
