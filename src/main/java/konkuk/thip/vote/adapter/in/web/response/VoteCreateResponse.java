package konkuk.thip.vote.adapter.in.web.response;

public record VoteCreateResponse(
        Long voteId
) {
    public static VoteCreateResponse of(Long voteId) {
        return new VoteCreateResponse(voteId);
    }
}
