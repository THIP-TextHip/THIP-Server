package konkuk.thip.vote.application.port.in.dto;

public record VoteCreateResult(
        Long voteId,
        Long roomId
) {
    public static VoteCreateResult of(Long voteId, Long roomId) {
        return new VoteCreateResult(voteId, roomId);
    }
}
