package konkuk.thip.room.adapter.in.web.response;

import java.util.List;

public record RoomGetDeadlinePopularResponse(
        List<RoomDto> deadlineRoomList,
        List<RoomDto> popularRoomList
) {
    public record RoomDto(
            Long roomId,
            String bookImageUrl,
            String roomName,
            int memberCount,
            String deadlineDate
    ) {
    }

    public static RoomGetDeadlinePopularResponse of(List<RoomDto> deadlineRoomList, List<RoomDto> popularRoomList) {
        return new RoomGetDeadlinePopularResponse(deadlineRoomList, popularRoomList);
    }
}
