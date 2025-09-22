package konkuk.thip.roompost.adapter.out.persistence.repository.vote;

import konkuk.thip.room.adapter.in.web.response.RoomPlayingOrExpiredDetailViewResponse;
import konkuk.thip.roompost.adapter.out.jpa.VoteJpaEntity;
import konkuk.thip.roompost.application.port.out.dto.VoteItemQueryDto;

import java.util.List;
import java.util.Set;

public interface VoteQueryRepository {

    List<VoteJpaEntity> findVotesByRoom(Long roomId, String type, Integer pageStart, Integer pageEnd, Long userId);

    List<RoomPlayingOrExpiredDetailViewResponse.CurrentVote> findTopParticipationVotesByRoom(Long roomId, int count);

    List<VoteItemQueryDto> mapVoteItemsByVoteIds(Set<Long> voteIds, Long userId);

    List<VoteItemQueryDto> findVoteItemsByVoteId(Long voteId, Long userId);
}
