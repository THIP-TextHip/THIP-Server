package konkuk.thip.user.adapter.out.persistence.repository.following;

import io.lettuce.core.dynamic.annotation.Param;
import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowingJpaRepository extends JpaRepository<FollowingJpaEntity, Long>, FollowingQueryRepository {

    @Query("SELECT COUNT(f) > 0 FROM FollowingJpaEntity f WHERE f.userJpaEntity.userId = :userId AND f.followingUserJpaEntity.userId = :followingUserId")
    boolean existsByUserIdAndFollowingUserId(@Param("userId") Long userId, @Param("followingUserId") Long followingUserId);

    @Query("SELECT COUNT(f) FROM FollowingJpaEntity f WHERE f.userJpaEntity.userId = :userId")
    int countFollowingByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM FollowingJpaEntity f WHERE f.userJpaEntity.userId = :userId OR f.followingUserJpaEntity.userId = :userId")
    void deleteAllByUserIdOrFollowingUserId(@Param("userId") Long userId);

    @Query("SELECT f.followingUserJpaEntity.userId FROM FollowingJpaEntity f WHERE f.userJpaEntity.userId = :userId")
    List<Long> findAllTargetUserIdsByUserId(@Param("userId") Long userId);
}
