package konkuk.thip.roompost.adapter.in.web.response;

import konkuk.thip.roompost.application.port.in.dto.record.RecordCreateResult;

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
