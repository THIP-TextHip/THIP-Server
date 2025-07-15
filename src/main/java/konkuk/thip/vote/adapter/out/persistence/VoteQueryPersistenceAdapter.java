package konkuk.thip.vote.adapter.out.persistence;

import konkuk.thip.room.adapter.in.web.response.RoomPlayingDetailViewResponse;
import konkuk.thip.room.domain.Room;
import konkuk.thip.vote.adapter.out.mapper.VoteMapper;
import konkuk.thip.vote.adapter.out.persistence.repository.VoteJpaRepository;
import konkuk.thip.vote.adapter.out.persistence.repository.VoteParticipantJpaRepository;
import konkuk.thip.vote.application.port.out.VoteQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class VoteQueryPersistenceAdapter implements VoteQueryPort {

    private final VoteJpaRepository voteJpaRepository;
    private final VoteMapper voteMapper;
    private final VoteParticipantJpaRepository voteParticipantJpaRepository;

    @Override
    public boolean isUserVoted(Long userId, Long voteItemId) {
        return voteParticipantJpaRepository.existsByUserJpaEntity_UserIdAndVoteItemJpaEntity_VoteItemId(userId, voteItemId);
    }

    @Override
    public List<RoomPlayingDetailViewResponse.CurrentVote> findTopParticipationVotesByRoom(Room room, int count) {
        return voteJpaRepository.findTopParticipationVotesByRoom(room.getId(), count);
    }
}
