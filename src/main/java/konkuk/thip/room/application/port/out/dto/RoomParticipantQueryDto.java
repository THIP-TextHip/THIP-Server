package konkuk.thip.room.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;
import konkuk.thip.room.domain.value.RoomStatus;
import lombok.Builder;
import org.springframework.util.Assert;

import java.time.LocalDate;

@Builder
public record RoomParticipantQueryDto(
        Long roomId,
        String bookImageUrl,
        String roomTitle,
        Integer memberCount,
        Double userPercentage,
        LocalDate startDate,  // 방 진행 시작일
        RoomStatus roomStatus
        ) {
    @QueryProjection
    public RoomParticipantQueryDto {
        Assert.notNull(roomId, "roomId must not be null");
        Assert.notNull(bookImageUrl, "bookImageUrl must not be null");
        Assert.notNull(roomTitle, "roomName must not be null");
        Assert.notNull(memberCount, "memberCount must not be null");
        Assert.notNull(userPercentage, "userPercentage must not be null");
        Assert.notNull(startDate, "startDate must not be null");
        Assert.notNull(roomStatus, "roomStatus must not be null");
    }
}
