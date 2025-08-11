package konkuk.thip.vote.adapter.out.persistence.repository;

import konkuk.thip.vote.adapter.out.jpa.VoteItemJpaEntity;
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
}
