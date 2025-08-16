package konkuk.thip.roompost.application.port.in.dto.vote;

public record VoteCreateResult(
        Long voteId,
        Long roomId
) {
    public static VoteCreateResult of(Long voteId, Long roomId) {
        return new VoteCreateResult(voteId, roomId);
    }
}
