package konkuk.thip.roompost.adapter.in.web.request;

import konkuk.thip.roompost.application.port.in.dto.record.RecordAiUsageResult;

public record RecordAiUsageResponse(
        Integer recordReviewCount,
        Integer recordCount
) {
    public static RecordAiUsageResponse of(RecordAiUsageResult result) {
        return new RecordAiUsageResponse(
                result.recordReviewCount(),
                result.recordCount()
        );
    }
}
