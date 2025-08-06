package konkuk.thip.room.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "내가 참여 중인 방 목록 조회 응답")
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

            @Schema(description = "방 진행 마감일 or 방 모집 마감일까지 남은 시간 (ex. \"3일 뒤\"), 완료된 방은 쓰레기값이 넘어갑니다. 무시해주세요.")
            String endDate,     // 방 진행 마감일 or 방 모집 마감일 (~ 뒤 형식)

            @Schema(description = "방 상태 : [모집중(=recruiting), 진행중(=playing), 완료된(=expired)] 중 하나")
            String type
    ) {}
}
