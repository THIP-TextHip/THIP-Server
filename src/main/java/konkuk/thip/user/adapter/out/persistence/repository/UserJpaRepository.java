package konkuk.thip.user.adapter.out.persistence.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long>, UserQueryRepository {

    /**
     * 소프트 딜리트 적용 대상 entity 단건 조회 메서드
     */
    Optional<UserJpaEntity> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000") // 5초
    })
    @Query("select u from UserJpaEntity u where u.userId = :userId")
    Optional<UserJpaEntity> findByUserIdWithLock(@Param("userId") Long userId);

    Optional<UserJpaEntity> findByOauth2Id(String oauth2Id);

    boolean existsByNickname(String nickname);

    boolean existsByNicknameAndUserIdNot(String nickname, Long userId);

    boolean existsByOauth2Id(String oauth2Id);

    @Query("SELECT f.followingUserJpaEntity.userId FROM FollowingJpaEntity f WHERE f.userJpaEntity.userId = :userId")
    List<Long> findAllTargetUserIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT f.userJpaEntity FROM FollowingJpaEntity f WHERE f.followingUserJpaEntity.userId = :userId")
    List<UserJpaEntity> findAllFollowersByUserId(@Param("userId") Long userId);
}
