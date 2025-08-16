package konkuk.thip.room.adapter.in.web.response;

import java.util.List;

public record RoomSearchResponse(
        List<RoomSearchDto> roomList,
        String nextCursor,
        boolean isLast
) {

    public record RoomSearchDto(
            Long roomId,
            String bookImageUrl,
            String roomName,
            int memberCount,
            int recruitCount,
            String deadlineDate,
            boolean isPublic
    ) {}
}
