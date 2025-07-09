package konkuk.thip.vote.application.port.out;

public interface VoteQueryPort {

    boolean isUserVoted(Long userId, Long voteId);

}
