package konkuk.thip.vote.application.service.dto;

public record VoteResult(
        Long voteItemId,
        Long roomId,
        Boolean type
) {
    public static VoteResult of(Long voteItemId, Long roomId, Boolean type) {
        return new VoteResult(voteItemId, roomId, type);
    }
}
