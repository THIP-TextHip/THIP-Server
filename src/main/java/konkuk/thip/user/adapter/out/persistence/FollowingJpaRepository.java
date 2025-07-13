package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowingJpaRepository extends JpaRepository<FollowingJpaEntity, Long> {
    int countByFollowingUserJpaEntity_UserId(Long userId);
}
