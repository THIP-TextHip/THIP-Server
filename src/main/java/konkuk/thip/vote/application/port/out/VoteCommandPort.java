package konkuk.thip.vote.application.port.out;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.vote.domain.Vote;
import konkuk.thip.vote.domain.VoteItem;

import java.util.List;
import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.VOTE_NOT_FOUND;

public interface VoteCommandPort {

    Long saveVote(Vote vote);

    void updateVote(Vote vote);

    void saveAllVoteItems(List<VoteItem> voteItems);

    List<VoteItem> findVoteItemsByVoteId(Long voteId);

    Optional<Vote> findById(Long id);

    default Vote getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException(VOTE_NOT_FOUND));
    }
}
