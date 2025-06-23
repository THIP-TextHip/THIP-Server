package konkuk.thip.domain.user.adapter.out.persistence;

import konkuk.thip.domain.user.adapter.out.jpa.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
}
