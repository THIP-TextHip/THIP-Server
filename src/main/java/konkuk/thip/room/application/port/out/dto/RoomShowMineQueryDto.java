package konkuk.thip.room.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record RoomShowMineQueryDto(
        Long roomId,
        String bookImageUrl,
        String roomName,
        int memberCount,
        LocalDate endDate       // 방 진행 마감일 or 방 모집 마감일
) {
    @QueryProjection
    public RoomShowMineQueryDto {}
}
