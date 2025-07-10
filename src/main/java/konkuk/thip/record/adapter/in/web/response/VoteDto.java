package konkuk.thip.record.adapter.in.web.response;

import konkuk.thip.user.domain.User;
import konkuk.thip.vote.domain.Vote;
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
) implements RecordSearchResponse.PostDto {
    @Override
    public String type() {
        return "VOTE";
    }

    public static VoteDto of(
            Vote vote, String postDate, User user, int likeCount, int commentCount, boolean isLiked, boolean isWriter,
            List<VoteItemDto> voteItems
    ) {
        return VoteDto.builder()
                .postDate(postDate)
                .page(vote.getPage())
                .userId(vote.getCreatorId())
                .nickName(user.getNickname())
                .profileImageUrl(user.getAlias().getImageUrl())
                .content(vote.getContent())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .isLiked(isLiked)
                .isWriter(isWriter)
                .voteId(vote.getId())
                .voteItems(voteItems)
                .build();
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