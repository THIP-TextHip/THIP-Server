package konkuk.thip.roompost.application.port.in.dto;

import lombok.Builder;

@Builder
public record RoomPostSearchQuery(
        Long roomId,
        String type,
        String sort,
        Integer pageStart,
        Integer pageEnd,
        Boolean isOverview,
        Boolean isPageFilter,
        String nextCursor,
        Long userId
) {
}
