package konkuk.thip.notification.adapter.out.persistence;

import konkuk.thip.notification.adapter.out.mapper.NotificationMapper;
import konkuk.thip.notification.application.port.out.NotificationCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationCommandPersistenceAdapter implements NotificationCommandPort {

    private final NotificationJpaRepository notificationJpaRepository;
    private final NotificationMapper notificationMapper;

}
