package konkuk.thip.vote.adapter.out.persistence;

import konkuk.thip.room.adapter.in.web.response.RoomPlayingDetailViewResponse;
import konkuk.thip.room.domain.Room;
import konkuk.thip.vote.adapter.out.persistence.repository.VoteJpaRepository;
import konkuk.thip.vote.application.port.out.VoteQueryPort;
import konkuk.thip.vote.application.port.out.dto.VoteItemQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Repository
@RequiredArgsConstructor
public class VoteQueryPersistenceAdapter implements VoteQueryPort {

    private final VoteJpaRepository voteJpaRepository;

    @Override
    public List<RoomPlayingDetailViewResponse.CurrentVote> findTopParticipationVotesByRoom(Room room, int count) {
        return voteJpaRepository.findTopParticipationVotesByRoom(room.getId(), count);
    }

    @Override
    public Map<Long, List<VoteItemQueryDto>> findVoteItemsByVoteIds(Set<Long> voteIds, Long userId) {
        return voteJpaRepository.mapVoteItemsByVoteIds(voteIds, userId).stream()
                .collect(
                        groupingBy(
                                VoteItemQueryDto::voteId,
                                Collectors.mapping(Function.identity(), Collectors.toList())
                        )
                );
    }

    @Override
    public List<VoteItemQueryDto> findVoteItemsByVoteId(Long voteId, Long userId) {
        return voteJpaRepository.findVoteItemsByVoteId(voteId, userId);
    }
}
