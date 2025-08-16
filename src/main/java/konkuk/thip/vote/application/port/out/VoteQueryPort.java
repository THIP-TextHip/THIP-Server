package konkuk.thip.vote.application.port.out;

import konkuk.thip.room.adapter.in.web.response.RoomPlayingDetailViewResponse;
import konkuk.thip.room.domain.Room;
import konkuk.thip.vote.application.port.out.dto.VoteItemQueryDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface VoteQueryPort {

    List<RoomPlayingDetailViewResponse.CurrentVote> findTopParticipationVotesByRoom(Room room, int count);

    Map<Long, List<VoteItemQueryDto>> findVoteItemsByVoteIds(Set<Long> voteIds, Long userId);

    List<VoteItemQueryDto> findVoteItemsByVoteId(Long voteId, Long userId);
}
