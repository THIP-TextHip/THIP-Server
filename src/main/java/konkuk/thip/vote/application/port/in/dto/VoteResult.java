package konkuk.thip.vote.application.port.in.dto;

import java.util.List;

public record VoteResult(
        Long postId,
        Long roomId,
        List<VoteItemDto> voteItems
) {
    public record VoteItemDto(
            Long voteItemId,
            String itemName,
            int percentage,
            Boolean isVoted
    ) {
        public static VoteItemDto of(Long voteItemId, String itemName, int percentage, Boolean isVoted) {
            return new VoteItemDto(voteItemId, itemName, percentage, isVoted);
        }
    }

    public static VoteResult of(Long postId, Long roomId, List<VoteItemDto> voteItems) {
        return new VoteResult(postId, roomId, voteItems);
    }
}
