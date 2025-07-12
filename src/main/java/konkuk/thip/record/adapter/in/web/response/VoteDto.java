package konkuk.thip.record.adapter.in.web.response;

import konkuk.thip.vote.domain.VoteItem;
import lombok.Builder;

import java.util.List;

@Builder
public record VoteDto(
        String postDate,
        int page,
        Long userId,
        String nickName,
        String profileImageUrl,
        String content,
        int likeCount,
        int commentCount,
        boolean isLiked,
        boolean isWriter,
        Long voteId,
        List<VoteItemDto> voteItems
) implements RecordSearchResponse.RecordSearchResult {
    @Override
    public String type() {
        return "VOTE";
    }

    public VoteDto withIsLikedAndVoteItems(boolean isLiked, List<VoteItemDto> voteItems) {
        return new VoteDto(
                postDate,
                page,
                userId,
                nickName,
                profileImageUrl,
                content,
                likeCount,
                commentCount,
                isLiked,
                isWriter,
                voteId,
                voteItems
        );
    }

    public record VoteItemDto(
            Long voteItemId,
            String itemName,
            int percentage,
            boolean isVoted
    ) {
        public static VoteItemDto of(VoteItem voteItem, int percentage, boolean isVoted) {
            return new VoteItemDto(
                    voteItem.getId(),
                    voteItem.getItemName(),
                    percentage,
                    isVoted
            );
        }
    }
}