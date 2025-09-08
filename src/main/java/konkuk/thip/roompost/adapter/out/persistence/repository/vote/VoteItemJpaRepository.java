package konkuk.thip.roompost.adapter.out.persistence.repository.vote;

import konkuk.thip.roompost.adapter.out.jpa.VoteItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VoteItemJpaRepository extends JpaRepository<VoteItemJpaEntity, Long> {

    List<VoteItemJpaEntity> findAllByVoteJpaEntity_PostId(Long voteId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM VoteItemJpaEntity vi WHERE vi.voteJpaEntity.postId = :voteId")
    void deleteAllByVoteId(@Param("voteId") Long voteId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM VoteItemJpaEntity vi WHERE vi.voteJpaEntity.postId IN :voteIds")
    void deleteAllByVoteIds(@Param("voteIds") List<Long> voteIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE VoteItemJpaEntity vi " +
            "SET vi.count = CASE WHEN vi.count > 0 THEN vi.count - 1 ELSE 0 END " +
            "WHERE vi.voteItemId IN :voteItemIds")
    void bulkDecrementLikeCount(@Param("voteItemIds") List<Long> voteItemIds);
}
