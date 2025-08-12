package konkuk.thip.record.adapter.in.web.response;

import konkuk.thip.record.application.port.in.dto.RecordCreateResult;

public record RecordCreateResponse(
        Long recordId,
        Long roomId
) {
    public static RecordCreateResponse of(RecordCreateResult recordCreateResult) {
        return new RecordCreateResponse(
                recordCreateResult.recordId(),
                recordCreateResult.roomId()
        );
    }
}
