package konkuk.thip.roompost.application.port.in.dto.record;

import lombok.Builder;

@Builder
public record RecordPinQuery(
        Long roomId,

        Long recordId,

        Long userId
) {
}
