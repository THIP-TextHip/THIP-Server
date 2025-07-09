package konkuk.thip.room.adapter.in.web.response;

import java.util.List;

public record RoomSearchResponse(
        List<RoomSearchResult> roomList,
        int page,       // 현재 페이지
        int size,       // 현재 페이지에 포함된 데이터 수
        boolean last,
        boolean first
) {

    public record RoomSearchResult(
            Long roomId,
            String bookImageUrl,
            String roomName,
            int memberCount,
            int recruitCount,
            String deadlineDate,
            String category
    ) {}
}
