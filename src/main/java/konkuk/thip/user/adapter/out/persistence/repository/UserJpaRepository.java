package konkuk.thip.user.adapter.out.persistence.repository;

import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long>, UserQueryRepository {
    Optional<UserJpaEntity> findByOauth2Id(String oauth2Id);
    boolean existsByNickname(String nickname);
    Optional<UserJpaEntity> findById(Long userId);

    boolean existsByNicknameAndUserIdNot(String nickname, Long userId);

    boolean existsByOauth2Id(String oauth2Id);
}
