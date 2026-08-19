package konkuk.thip.user.adapter.out.persistence.repository.block;

import konkuk.thip.user.adapter.out.jpa.UserBlockJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBlockJpaRepository extends JpaRepository<UserBlockJpaEntity, Long>, UserBlockQueryRepository {

    @Query("SELECT COUNT(b) > 0 FROM UserBlockJpaEntity b " +
            "WHERE (b.userJpaEntity.userId = :userId AND b.blockedUserJpaEntity.userId = :targetUserId) " +
            "OR (b.userJpaEntity.userId = :targetUserId AND b.blockedUserJpaEntity.userId = :userId)")
    boolean existsBlockBetween(@Param("userId") Long userId, @Param("targetUserId") Long targetUserId);

    @Query("SELECT COUNT(b) FROM UserBlockJpaEntity b WHERE b.userJpaEntity.userId = :userId")
    int countBlockedUsersByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM UserBlockJpaEntity b " +
            "WHERE b.userJpaEntity.userId = :userId OR b.blockedUserJpaEntity.userId = :userId")
    void deleteAllByUserIdOrBlockedUserId(@Param("userId") Long userId);
}
