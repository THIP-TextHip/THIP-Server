package konkuk.thip.room.adapter.in.web.response;

import java.time.LocalDate;
import java.util.List;

public record RoomShowMineResponse(
        List<MyRoom> roomList,
        int size,       // 현제 페이지에 포함된 데이터 수
        boolean last,
        LocalDate nextCursorDate,       // 다음 페이지 시작 커서의 endDate 값
        Long nextCursorId               // 다음 페이지 시작 커서의 roomId 값
) {
    public record MyRoom(
            Long roomId,
            String bookImageUrl,
            String roomName,
            int memberCount,
            String endDate      // 방 진행 마감일 or 방 모집 마감일 (~ 뒤 형식)
    ) {}
}
