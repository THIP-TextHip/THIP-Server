package konkuk.thip.room.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;
import jakarta.annotation.Nullable;
import lombok.Builder;
import org.springframework.util.Assert;

import java.time.LocalDate;

@Builder
public record RoomQueryDto(
        Long roomId,
        String bookImageUrl,
        String roomName,
        Integer recruitCount, // 방 최대 인원 수
        Integer memberCount,
        @Nullable LocalDate startDate,    // 방 진행 시작일
        LocalDate endDate,       // 방 진행 마감일 or 방 모집 마감일
        Boolean isPublic        // 공개방 여부
) {
    @QueryProjection
    public RoomQueryDto {
        Assert.notNull(roomId, "roomId must not be null");
        Assert.notNull(bookImageUrl, "bookImageUrl must not be null");
        Assert.notNull(roomName, "roomName must not be null");
        Assert.notNull(endDate, "endDate must not be null");
        Assert.notNull(recruitCount, "recruitCount must not be null");
        Assert.notNull(memberCount, "memberCount must not be null");
    }

    // 내가 참여한 모임방(모집중, 진행중, 모집+진행중, 완료된) 조회 시 활용
    @QueryProjection
    public RoomQueryDto(
            Long roomId,
            String bookImageUrl,
            String roomName,
            Integer recruitCount,
            Integer memberCount,
            LocalDate endDate
    ) {
        this(roomId, bookImageUrl, roomName, recruitCount, memberCount, null, endDate, null);
    }

    // 방 검색 시 활용
    @QueryProjection
    public RoomQueryDto(
            Long roomId,
            String bookImageUrl,
            String roomName,
            Integer recruitCount,
            Integer memberCount,
            LocalDate endDate,
            Boolean isPublic
    ) {
        this(roomId, bookImageUrl, roomName, recruitCount, memberCount, null, endDate, isPublic);
    }
}
