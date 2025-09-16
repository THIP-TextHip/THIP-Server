package konkuk.thip.room.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

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
            @Schema(description = "[진행중인 방]에서 유저의 방 진행도 --> 모집중인 방은 쓰레기값이 넘어갑니다. 무시해주세요.",
                    example = "35")
            int userPercentage,
            @Schema(description = "[모집중인 방]에서 방 모집 마감일까지 남은 시간 --> 진행중인 방은 쓰레기값이 넘어갑니다. 무시해주세요.",
                    example = "3일")
            String deadlineDate  // 방 모집 마감일 (~일/시 형식)
    ) {}
    public static RoomGetHomeJoinedListResponse of(List<RoomGetHomeJoinedListResponse.JoinedRoomInfo> roomList,
                                                   String nickname, String nextCursor, boolean isLast){
    return new RoomGetHomeJoinedListResponse(roomList, nickname, nextCursor, isLast);}
}