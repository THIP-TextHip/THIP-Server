package konkuk.thip.room.adapter.in.web.response;

import java.util.List;

public record RoomGetDeadlinePopularRecentResponse(
        List<RoomGetDeadlinePopularRecentDto> deadlineRoomList,
        List<RoomGetDeadlinePopularRecentDto> popularRoomList,
        List<RoomGetDeadlinePopularRecentDto> recentRoomList
) {
    public record RoomGetDeadlinePopularRecentDto(
            Long roomId,
            String bookImageUrl,
            String roomName,
            int recruitCount, // 방 최대 인원 수
            int memberCount,
            String deadlineDate
    ) {
    }

    public static RoomGetDeadlinePopularRecentResponse of(List<RoomGetDeadlinePopularRecentDto> deadlineRoomList,
                                                          List<RoomGetDeadlinePopularRecentDto> popularRoomList,
                                                          List<RoomGetDeadlinePopularRecentDto> recentRoomList) {
        return new RoomGetDeadlinePopularRecentResponse(deadlineRoomList, popularRoomList, recentRoomList);
    }
}
