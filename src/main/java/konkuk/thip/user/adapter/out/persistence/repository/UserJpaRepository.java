package konkuk.thip.user.adapter.out.persistence.repository;

import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long>, UserQueryRepository {

    /**
     * 소프트 딜리트 적용 대상 entity 단건 조회 메서드
     */
    Optional<UserJpaEntity> findByUserId(Long userId);

    Optional<UserJpaEntity> findByOauth2Id(String oauth2Id);
    boolean existsByNickname(String nickname);

    boolean existsByNicknameAndUserIdNot(String nickname, Long userId);

    boolean existsByOauth2Id(String oauth2Id);
}
