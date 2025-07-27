package konkuk.thip.record.application.port.in.dto;

import lombok.Builder;

@Builder
public record RecordSearchQuery(
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
