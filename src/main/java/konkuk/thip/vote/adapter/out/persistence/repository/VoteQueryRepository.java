package konkuk.thip.vote.adapter.out.persistence.repository;

import konkuk.thip.room.adapter.in.web.response.RoomPlayingDetailViewResponse;
import konkuk.thip.vote.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.vote.application.port.out.dto.VoteItemQueryDto;

import java.util.List;
import java.util.Set;

public interface VoteQueryRepository {

    List<VoteJpaEntity> findVotesByRoom(Long roomId, String type, Integer pageStart, Integer pageEnd, Long userId);

    List<RoomPlayingDetailViewResponse.CurrentVote> findTopParticipationVotesByRoom(Long roomId, int count);

    List<VoteItemQueryDto> mapVoteItemsByVoteIds(Set<Long> voteIds, Long userId);

    List<VoteItemQueryDto> findVoteItemsByVoteId(Long voteId, Long userId);
}
