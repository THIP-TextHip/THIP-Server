package konkuk.thip.vote.adapter.in.web.response;

import konkuk.thip.vote.application.service.dto.VoteResult;

public record VoteResponse(
        Long voteItemId,
        Long roomId,
        Boolean type
) {
    public static VoteResponse of(VoteResult voteResult) {
        return new VoteResponse(voteResult.voteItemId(), voteResult.roomId(), voteResult.type());
    }
}
