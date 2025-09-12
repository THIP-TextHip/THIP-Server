package konkuk.thip.room.adapter.in.web.response;

import java.util.List;

public record RoomGetHomeJoinedListResponse(
        List<JoinedRoomInfo> roomList,
        String nickname,
        String nextCursor,
        boolean isLast
) {
    public record JoinedRoomInfo(
            Long roomId,
            String bookImageUrl,
            String roomTitle,
            int memberCount,
            int userPercentage
    ) {}
    public static RoomGetHomeJoinedListResponse of(List<RoomGetHomeJoinedListResponse.JoinedRoomInfo> roomList,
                                                   String nickname, String nextCursor, boolean isLast){
    return new RoomGetHomeJoinedListResponse(roomList, nickname, nextCursor, isLast);}
}