package konkuk.thip.room.adapter.in.web.response;

import java.util.List;

public record RoomShowMineResponse(
        List<MyRoom> roomList,
        String nextCursor,
        boolean isLast
) {
    public record MyRoom(
            Long roomId,
            String bookImageUrl,
            String roomName,
            int memberCount,
            String endDate      // 방 진행 마감일 or 방 모집 마감일 (~ 뒤 형식)
    ) {}
}
