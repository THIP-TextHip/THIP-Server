package konkuk.thip.user.adapter.out.persistence.repository.following;

import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserJpaEntity u " +
                "SET u.followerCount = CASE WHEN u.followerCount > 0 THEN u.followerCount - 1 ELSE 0 END " +
                "WHERE u.userId IN :targetUserIds"
    )
    void bulkDecrementFollowerCount(@Param("targetUserIds") List<Long> targetUserIds);
}
