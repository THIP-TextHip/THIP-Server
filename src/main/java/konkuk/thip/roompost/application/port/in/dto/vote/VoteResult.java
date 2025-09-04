package konkuk.thip.roompost.application.port.in.dto.vote;

import java.util.List;

public record VoteResult(
        Long postId,
        Long roomId,
        List<VoteItemDto> voteItems
) {
    public record VoteItemDto(
            Long voteItemId,
            String itemName,
            int count,
            Boolean isVoted
    ) {
        public static VoteItemDto of(Long voteItemId, String itemName, int count, Boolean isVoted) {
            return new VoteItemDto(voteItemId, itemName, count, isVoted);
        }
    }

    public static VoteResult of(Long postId, Long roomId, List<VoteItemDto> voteItems) {
        return new VoteResult(postId, roomId, voteItems);
    }
}
