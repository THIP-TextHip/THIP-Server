package konkuk.thip.roompost.adapter.out.persistence.repository.vote;

import konkuk.thip.roompost.adapter.out.jpa.VoteParticipantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteParticipantJpaRepository extends JpaRepository<VoteParticipantJpaEntity, Long>, VoteParticipantQueryRepository {
    @Query("SELECT vp FROM VoteParticipantJpaEntity vp WHERE vp.userJpaEntity.userId = :userId AND vp.voteItemJpaEntity.voteItemId = :voteItemId")
    Optional<VoteParticipantJpaEntity> findVoteParticipantByUserIdAndVoteItemId(@Param("userId") Long userId,@Param("voteItemId") Long voteItemId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
       DELETE FROM VoteParticipantJpaEntity vp
       WHERE vp.voteItemJpaEntity.voteItemId IN (
            SELECT vi.voteItemId
            FROM VoteItemJpaEntity vi
            WHERE vi.voteJpaEntity.postId = :voteId
       )
       """)
    void deleteAllByVoteId(@Param("voteId") Long voteId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
       DELETE FROM VoteParticipantJpaEntity vp
       WHERE vp.voteItemJpaEntity.voteItemId IN (
            SELECT vi.voteItemId
            FROM VoteItemJpaEntity vi
            WHERE vi.voteJpaEntity.postId IN :voteIds
       )
       """)
    void deleteAllByVoteIds(@Param("voteIds") List<Long> voteIds);

    @Query("SELECT vp.voteItemJpaEntity.voteItemId FROM VoteParticipantJpaEntity vp WHERE vp.userJpaEntity.userId = :userId")
    List<Long> findAllVoteItemIdsByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM VoteParticipantJpaEntity vp WHERE vp.userJpaEntity.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
