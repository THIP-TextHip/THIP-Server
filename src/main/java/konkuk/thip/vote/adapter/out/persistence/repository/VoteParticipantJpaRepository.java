package konkuk.thip.vote.adapter.out.persistence.repository;

import konkuk.thip.vote.adapter.out.jpa.VoteParticipantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteParticipantJpaRepository extends JpaRepository<VoteParticipantJpaEntity, Long> {
    boolean existsByUserJpaEntity_UserIdAndVoteItemJpaEntity_VoteItemId(Long userId, Long voteItemId);
}
