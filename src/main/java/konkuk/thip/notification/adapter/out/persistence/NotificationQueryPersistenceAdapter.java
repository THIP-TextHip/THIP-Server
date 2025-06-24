package konkuk.thip.notification.adapter.out.persistence;

import konkuk.thip.notification.adapter.out.mapper.NotificationMapper;
import konkuk.thip.notification.application.port.out.NotificationQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationQueryPersistenceAdapter implements NotificationQueryPort {

    private final NotificationJpaRepository jpaRepository;
    private final NotificationMapper notificationMapper;

}
