package konkuk.thip.vote.adapter.in.web.response;

import konkuk.thip.vote.application.port.in.dto.VoteCreateResult;

public record VoteCreateResponse(
        Long voteId,
        Long roomId
) {
    public static VoteCreateResponse of(VoteCreateResult voteCreateResult) {
        return new VoteCreateResponse(
                voteCreateResult.voteId(),
                voteCreateResult.roomId()
        );
    }
}
