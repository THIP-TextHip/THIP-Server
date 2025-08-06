package konkuk.thip.book.adapter.in.web.response;

import java.util.List;

public record BookRecruitingRoomsResponse(
        List<RecruitingRoomDto> recruitingRoomList,
        Integer totalRoomCount,
        String nextCursor,
        boolean isLast
) {
    public static BookRecruitingRoomsResponse of(List<RecruitingRoomDto> recruitingRoomList, Integer totalRoomCount, String nextCursor, boolean isLast) {
        return new BookRecruitingRoomsResponse(recruitingRoomList, totalRoomCount, nextCursor, isLast);
    }

    public record RecruitingRoomDto(
            Long roomId,
            String bookImageUrl,
            String roomName,
            int memberCount,
            int recruitCount,
            String deadlineEndDate
    ) {
        public static RecruitingRoomDto of(Long roomId, String bookImageUrl, String roomName, int memberCount, int recruitCount, String deadlineEndDate) {
            return new RecruitingRoomDto(roomId, bookImageUrl, roomName, memberCount, recruitCount, deadlineEndDate);
        }
    }
}
