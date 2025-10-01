package konkuk.thip.notification.application.service;

import konkuk.thip.notification.application.port.in.NotificationExistsUncheckedUseCase;
import konkuk.thip.notification.application.port.out.NotificationQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationExistsUncheckedService implements NotificationExistsUncheckedUseCase {

    private final NotificationQueryPort notificationQueryPort;

    @Override
    @Transactional(readOnly = true)
    public boolean existsUnchecked(Long userId) {
        return notificationQueryPort.existsUnchecked(userId);
    }
}
