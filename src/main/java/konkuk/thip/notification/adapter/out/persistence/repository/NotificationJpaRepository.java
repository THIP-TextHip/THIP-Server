package konkuk.thip.notification.adapter.out.persistence.repository;

import konkuk.thip.notification.adapter.out.jpa.NotificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, Long>, NotificationQueryRepository {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE NotificationJpaEntity n SET n.isChecked = true " +
            "WHERE n.userJpaEntity.userId = :userId AND n.isChecked = false")
    int markAllAsCheckedByUserId(@Param("userId") Long userId);
}