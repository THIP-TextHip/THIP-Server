package konkuk.thip.vote.adapter.in.web.response;

import konkuk.thip.vote.application.port.in.dto.VoteResult;

import java.util.List;

public record VoteResponse(
        Long postId,
        Long roomId,
        List<VoteResult.VoteItemDto> voteItems
) {

    public static VoteResponse of(Long postId, Long roomId, List<VoteResult.VoteItemDto> voteItems) {
        return new VoteResponse(postId, roomId, voteItems);
    }
}
