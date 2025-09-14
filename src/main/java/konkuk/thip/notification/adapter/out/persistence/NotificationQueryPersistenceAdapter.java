package konkuk.thip.notification.adapter.out.persistence;

import konkuk.thip.notification.adapter.out.mapper.NotificationMapper;
import konkuk.thip.notification.adapter.out.persistence.repository.NotificationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationQueryPersistenceAdapter {

    private final NotificationJpaRepository jpaRepository;
    private final NotificationMapper notificationMapper;

}
