package konkuk.thip.book.adapter.in.web.response;

import java.util.List;

public record BookRecruitingRoomsResponse(
        List<BookRecruitingRoomDto> recruitingRoomList,
        Integer totalRoomCount,
        String nextCursor,
        boolean isLast
) {
    public static BookRecruitingRoomsResponse of(List<BookRecruitingRoomDto> recruitingRoomList, Integer totalRoomCount, String nextCursor, boolean isLast) {
        return new BookRecruitingRoomsResponse(recruitingRoomList, totalRoomCount, nextCursor, isLast);
    }

    public record BookRecruitingRoomDto(
            Long roomId,
            String bookImageUrl,
            String roomName,
            int memberCount,
            int recruitCount,
            String deadlineEndDate,
            boolean isPublic
    ) {
    }
}
