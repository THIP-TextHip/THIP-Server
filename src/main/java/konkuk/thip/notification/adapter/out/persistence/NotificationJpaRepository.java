package konkuk.thip.notification.adapter.out.persistence;

import konkuk.thip.notification.adapter.out.jpa.NotificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, Long> {
}