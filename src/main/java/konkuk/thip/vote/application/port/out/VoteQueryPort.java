package konkuk.thip.vote.application.port.out;

import konkuk.thip.room.adapter.in.web.response.RoomPlayingDetailViewResponse;
import konkuk.thip.room.domain.Room;

import java.util.List;

public interface VoteQueryPort {

    boolean isUserVoted(Long userId, Long voteId);

    List<RoomPlayingDetailViewResponse.CurrentVote> findTopParticipationVotesByRoom(Room room, int count);
}
