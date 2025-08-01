package konkuk.thip.vote.adapter.out.persistence.repository;

import konkuk.thip.vote.adapter.out.jpa.VoteParticipantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteParticipantJpaRepository extends JpaRepository<VoteParticipantJpaEntity, Long>, VoteParticipantQueryRepository {
    @Query("SELECT vp FROM VoteParticipantJpaEntity vp WHERE vp.userJpaEntity.userId = :userId AND vp.voteItemJpaEntity.voteItemId = :voteItemId")
    Optional<VoteParticipantJpaEntity> findVoteParticipantByUserIdAndVoteItemId(Long userId, Long voteItemId);
}
