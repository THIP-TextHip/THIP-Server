package konkuk.thip.room.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import org.springframework.util.Assert;

import java.time.LocalDate;

@Builder
public record RoomQueryDto(
        Long roomId,
        String bookImageUrl,
        String roomName,
        int memberCount,
        LocalDate endDate       // 방 진행 마감일 or 방 모집 마감일
) {
    @QueryProjection
    public RoomQueryDto {
        Assert.notNull(roomId, "roomId must not be null");
        Assert.notNull(bookImageUrl, "bookImageUrl must not be null");
        Assert.notNull(roomName, "roomName must not be null");
        Assert.notNull(endDate, "endDate must not be null");
        Assert.isTrue(memberCount >= 0, "memberCount must be greater than or equal to 0");
        Assert.notNull(endDate, "endDate must not be null");
    }
}
