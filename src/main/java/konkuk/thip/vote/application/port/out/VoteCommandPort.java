package konkuk.thip.vote.application.port.out;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.vote.domain.Vote;
import konkuk.thip.vote.domain.VoteItem;
import konkuk.thip.vote.domain.VoteParticipant;

import java.util.List;
import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.VOTE_ITEM_NOT_FOUND;
import static konkuk.thip.common.exception.code.ErrorCode.VOTE_NOT_FOUND;

public interface VoteCommandPort {

    Long saveVote(Vote vote);

    void updateVote(Vote vote);

    void saveAllVoteItems(List<VoteItem> voteItems);

    Optional<Vote> findById(Long id);

    default Vote getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException(VOTE_NOT_FOUND));
    }

    Optional<VoteItem> findVoteItemById(Long id);

    default VoteItem getVoteItemByIdOrThrow(Long id) {
        return findVoteItemById(id)
                .orElseThrow(() -> new EntityNotFoundException(VOTE_ITEM_NOT_FOUND));
    }

    Optional<VoteParticipant> findVoteParticipantByUserIdAndVoteId(Long userId, Long voteId);

    Optional<VoteParticipant> findVoteParticipantByUserIdAndVoteItemId(Long userId, Long voteItemId);

    void updateVoteParticipant(VoteParticipant voteParticipant);

    void saveVoteParticipant(VoteParticipant voteParticipant);

    void deleteVoteParticipant(VoteParticipant voteParticipant);

    void updateVoteItem(VoteItem voteItem);
}
