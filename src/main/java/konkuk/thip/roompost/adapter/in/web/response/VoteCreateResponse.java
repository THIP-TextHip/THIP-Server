package konkuk.thip.roompost.adapter.in.web.response;

import konkuk.thip.roompost.application.port.in.dto.vote.VoteCreateResult;

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
