package konkuk.thip.room.adapter.in.web.response;

import java.util.List;

public record RoomGetDeadlinePopularResponse(
        List<RoomGetDeadlinePopularDto> deadlineRoomList,
        List<RoomGetDeadlinePopularDto> popularRoomList
) {
    public record RoomGetDeadlinePopularDto(
            Long roomId,
            String bookImageUrl,
            String roomName,
            int recruitCount, // 방 최대 인원 수
            int memberCount,
            String deadlineDate
    ) {
    }

    public static RoomGetDeadlinePopularResponse of(List<RoomGetDeadlinePopularDto> deadlineRoomList, List<RoomGetDeadlinePopularDto> popularRoomList) {
        return new RoomGetDeadlinePopularResponse(deadlineRoomList, popularRoomList);
    }
}
