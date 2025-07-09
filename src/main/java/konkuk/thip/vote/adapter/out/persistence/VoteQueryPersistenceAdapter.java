package konkuk.thip.vote.adapter.out.persistence;

import konkuk.thip.vote.adapter.out.mapper.VoteMapper;
import konkuk.thip.vote.application.port.out.VoteQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VoteQueryPersistenceAdapter implements VoteQueryPort {

    private final VoteJpaRepository voteJpaRepository;
    private final VoteMapper voteMapper;
    private final UserVoteJpaRepository userVoteJpaRepository;

    @Override
    public boolean isUserVoted(Long userId, Long voteItemId) {
        return userVoteJpaRepository.existsByUserJpaEntity_UserIdAndVoteItemJpaEntity_VoteItemId(userId, voteItemId);
    }
}
