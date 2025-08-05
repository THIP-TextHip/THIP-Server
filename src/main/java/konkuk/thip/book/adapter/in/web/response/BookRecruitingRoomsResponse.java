package konkuk.thip.book.adapter.in.web.response;

import java.util.List;

public record BookRecruitingRoomsResponse(
        List<RecruitingRoomDto> recruitingRoomList,
        String nextCursor,
        boolean isLast
) {
    public static BookRecruitingRoomsResponse of(List<RecruitingRoomDto> recruitingRoomList, String nextCursor, boolean isLast) {
        return new BookRecruitingRoomsResponse(recruitingRoomList, nextCursor, isLast);
    }

    public record RecruitingRoomDto(
            String bookImageUrl,
            String title,
            int memberCount,
            int recruitCount,
            String recruitEndDate
    ) {
        public static RecruitingRoomDto of(String bookImageUrl, String title, int memberCount, int recruitCount, String recruitEndDate) {
            return new RecruitingRoomDto(bookImageUrl, title, memberCount, recruitCount, recruitEndDate);
        }
    }
}
