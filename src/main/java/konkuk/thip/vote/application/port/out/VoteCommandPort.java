package konkuk.thip.vote.application.port.out;

import konkuk.thip.vote.domain.Vote;
import konkuk.thip.vote.domain.VoteItem;

import java.util.List;

public interface VoteCommandPort {

    Long saveVote(Vote vote);

    void saveAllVoteItems(List<VoteItem> voteItems);
}
