package konkuk.thip.record.adapter.in.web.response;

import lombok.Builder;

import java.util.List;

@Builder
public record RecordSearchResponse(
    List<PostDto> postList,
    String nextCursor,
    Boolean isLast
){
    @Builder
    public record PostDto(
            Long postId,
            String postDate,
            String postType,
            int page,
            Long userId,
            String nickName,
            String profileImageUrl,
            String content,
            int likeCount,
            int commentCount,
            boolean isLiked,
            boolean isWriter,
            boolean isLocked,
            List<VoteItemDto> voteItems
    ) {
        public record VoteItemDto(
                Long voteItemId,
                String itemName,
                int percentage,
                boolean isVoted
        ) {
            public static VoteItemDto of(Long voteItemId, String itemName, int percentage, boolean isVoted) {
                return new VoteItemDto(voteItemId, itemName, percentage, isVoted);
            }
        }
    }
}
