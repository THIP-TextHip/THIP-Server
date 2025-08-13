package konkuk.thip.room.adapter.in.web.response;

import lombok.Builder;

import java.util.List;

@Builder
public record RoomPlayingDetailViewResponse(
        boolean isHost,
        Long roomId,
        String roomName,
        String roomImageUrl,
        boolean isPublic,
        String progressStartDate,
        String progressEndDate,
        String category,
        String categoryColor,
        String roomDescription,
        int memberCount,
        int recruitCount,
        String isbn,
        String bookTitle,
        String authorName,
        int currentPage,
        double userPercentage,
        List<CurrentVote> currentVotes
) {
    public record CurrentVote(
            String content,
            int page,
            boolean isOverview,
            List<VoteItem> voteItems
    ) {
        public record VoteItem(
                String itemName
        ) {}
    }
}
