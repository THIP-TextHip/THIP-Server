package konkuk.thip.roompost.adapter.in.web.response;

import konkuk.thip.roompost.application.port.in.dto.record.RecordReviewCreateResult;

public record RecordReviewCreateResponse(
        String content,
        int count
) {
    public static RecordReviewCreateResponse of(RecordReviewCreateResult result) {
        return new RecordReviewCreateResponse(result.content(), result.reviewCount());
    }
}
