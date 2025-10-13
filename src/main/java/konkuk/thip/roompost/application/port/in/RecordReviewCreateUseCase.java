package konkuk.thip.roompost.application.port.in;

import konkuk.thip.roompost.application.port.in.dto.record.RecordReviewCreateResult;

public interface RecordReviewCreateUseCase {

    RecordReviewCreateResult createAiRecordReview(Long roomId, Long userId);
}
