package konkuk.thip.notification.adapter.out.persistence.repository;

import konkuk.thip.notification.adapter.out.jpa.FcmTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FcmTokenJpaRepository extends JpaRepository<FcmTokenJpaEntity, Long> {

    @Query("SELECT f FROM FcmTokenJpaEntity f WHERE f.deviceId = :deviceId")
    Optional<FcmTokenJpaEntity> findByDeviceId(String deviceId);

    @Query("SELECT f FROM FcmTokenJpaEntity f WHERE f.userJpaEntity.userId = :userId AND f.isEnabled = true")
    List<FcmTokenJpaEntity> findByUserIdAndIsEnabledTrue(Long userId);

    Optional<FcmTokenJpaEntity> findByFcmTokenId(Long id);
}
