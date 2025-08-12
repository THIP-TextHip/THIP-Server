package konkuk.thip.vote.adapter.out.persistence.repository;

import konkuk.thip.vote.adapter.out.jpa.VoteParticipantJpaEntity;

import java.util.Optional;

public interface VoteParticipantQueryRepository {

    Optional<VoteParticipantJpaEntity> findVoteParticipantByUserIdAndVoteId(Long userId, Long voteId);
}
