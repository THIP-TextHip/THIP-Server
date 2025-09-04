package konkuk.thip.roompost.adapter.in.web.response;

import lombok.Builder;

import java.util.List;

@Builder
public record RoomPostSearchResponse(
    List<RoomPostSearchDto> postList,
    Long roomId,
    String isbn,
    boolean isOverviewEnabled,
    String nextCursor,
    Boolean isLast
){
    @Builder
    public record RoomPostSearchDto(
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
            boolean isOverview,
            boolean isLiked,
            boolean isWriter,
            boolean isLocked,
            List<VoteItemDto> voteItems
    ) {
        public record VoteItemDto(
                Long voteItemId,
                String itemName,
                int count,
                boolean isVoted
        ) {
            public static VoteItemDto of(Long voteItemId, String itemName, int count, boolean isVoted) {
                return new VoteItemDto(voteItemId, itemName, count, isVoted);
            }
        }
    }
}
