package konkuk.thip.notification.adapter.out.persistence.repository;

import konkuk.thip.notification.adapter.out.jpa.NotificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, Long>, NotificationQueryRepository {
}