package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long>, UserQueryRepository {

    boolean existsByNickname(String nickname);
    Optional<UserJpaEntity> findById(Long userId);
}
