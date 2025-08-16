package konkuk.thip.roompost.adapter.out.persistence.repository.vote;

import konkuk.thip.roompost.adapter.out.jpa.VoteParticipantJpaEntity;

import java.util.Optional;

public interface VoteParticipantQueryRepository {

    Optional<VoteParticipantJpaEntity> findVoteParticipantByUserIdAndVoteId(Long userId, Long voteId);
}
