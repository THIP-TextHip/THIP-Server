package konkuk.thip.record.application.port.in.dto;

import lombok.Builder;

@Builder
public record RecordSearchQuery(
        Long roomId,
        String type,
        String sort,
        Integer pageStart,
        Integer pageEnd,
        Integer pageNum,
        Long userId) {
}
