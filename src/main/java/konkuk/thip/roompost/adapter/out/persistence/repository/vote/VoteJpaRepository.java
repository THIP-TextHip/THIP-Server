package konkuk.thip.roompost.adapter.out.persistence.repository.vote;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import konkuk.thip.roompost.adapter.out.jpa.VoteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.QueryHints;

public interface VoteJpaRepository extends JpaRepository<VoteJpaEntity, Long>, VoteQueryRepository {

    /**
     * 소프트 딜리트 적용 대상 entity 단건 조회 메서드
     */
    Optional<VoteJpaEntity> findByPostId(Long postId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM VoteJpaEntity v WHERE v.postId = :postId")
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    Optional<VoteJpaEntity> findByPostIdForUpdate(@Param("postId") Long postId);

    @Query("SELECT v.postId FROM VoteJpaEntity v WHERE v.userJpaEntity.userId = :userId")
    List<Long> findVoteIdsByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE VoteJpaEntity v SET v.status = 'INACTIVE' WHERE v.userJpaEntity.userId = :userId")
    void softDeleteAllByUserId(@Param("userId") Long userId);
}
