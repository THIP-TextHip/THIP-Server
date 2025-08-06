package konkuk.thip.room.adapter.in.web.response;

import lombok.Builder;

import java.util.List;

public record RoomRecruitingDetailViewResponse(
        boolean isHost,
        boolean isJoining,
        Long roomId,
        String roomName,
        String roomImageUrl,
        boolean isPublic,
        String progressStartDate,
        String progressEndDate,
        String recruitEndDate,
        String category,
        String roomDescription,
        int memberCount,
        int recruitCount,
        String isbn,
        String bookImageUrl,
        String bookTitle,
        String authorName,
        String bookDescription,
        String publisher,
        List<RecommendRoom> recommendRooms
) {
    @Builder
    public record RecommendRoom(
            Long roomId,
            String roomImageUrl,
            String roomName,
            int memberCount,
            int recruitCount,
            String recruitEndDate
    ) {}
}
