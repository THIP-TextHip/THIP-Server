package konkuk.thip.roompost.adapter.in.web.response;

import lombok.Builder;

import java.util.List;

@Builder
public record RoomPostSearchResponse(
    List<RoomPostDto> postList,
    Long roomId,
    String isbn,
    boolean isOverviewEnabled,
    String nextCursor,
    Boolean isLast
){
    @Builder
    public record RoomPostDto(
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
                int percentage,
                boolean isVoted
        ) {
            public static VoteItemDto of(Long voteItemId, String itemName, int percentage, boolean isVoted) {
                return new VoteItemDto(voteItemId, itemName, percentage, isVoted);
            }
        }
    }
}
