package konkuk.thip.record.adapter.in.web.response;

import java.util.List;

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
) implements RecordSearchResponse.PostDto {
    @Override
    public String type() {
        return "VOTE";
    }

    public record VoteItemDto(
            Long voteItemId,
            String itemName,
            int percentage,
            boolean isVoted
    ) {}
}