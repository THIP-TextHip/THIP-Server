package konkuk.thip.user.adapter.out.persistence.repository.following;

import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowingJpaRepository extends JpaRepository<FollowingJpaEntity, Long>, FollowingQueryRepository {

    @Query("SELECT COUNT(f) > 0 FROM FollowingJpaEntity f WHERE f.userJpaEntity.userId = :userId AND f.followingUserJpaEntity.userId = :followingUserId")
    boolean existsByUserIdAndFollowingUserId(Long userId, Long followingUserId);

    @Query("SELECT COUNT(f) FROM FollowingJpaEntity f WHERE f.userJpaEntity.userId = :userId")
    int countFollowingByUserId(Long userId);
}
