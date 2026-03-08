package konkuk.thip.config;

import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TestUserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    Optional<UserJpaEntity> findByUserId(Long userId);
}
