package konkuk.thip.roompost.application.service;

import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.ai.application.out.GeminiLoadPort;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import konkuk.thip.roompost.application.port.in.RecordReviewCreateUseCase;
import konkuk.thip.roompost.application.port.in.dto.record.RecordReviewCreateResult;
import konkuk.thip.roompost.application.port.out.RecordQueryPort;
import konkuk.thip.roompost.domain.Record;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordReviewCreateService implements RecordReviewCreateUseCase {

    private final RoomParticipantValidator roomParticipantValidator;
    private final RecordQueryPort recordQueryPort;
    private final UserCommandPort userCommandPort;
    private final GeminiLoadPort geminiQueryPort;
    private final BookCommandPort bookCommandPort;

    private final static int MIN_REVIEW_LENGTH = 600;  // 독후감 최소 길이
    private final static int MAX_REVIEW_LENGTH = 900; // 독후감 최대 길이

    @Override
    public RecordReviewCreateResult createAiRecordReview(Long roomId, Long userId) {
        roomParticipantValidator.validateUserIsRoomMember(roomId, userId);

        // 1. 필요한 엔티티 조회
        List<Record> records = recordQueryPort.findAllByRoomIdAndUserId(roomId, userId);
        User user = userCommandPort.findById(userId);
        Book book = bookCommandPort.findBookByRoomId(roomId);

        // 2. 유저가 독후감 생성이 가능한 상태인지 유효성 검증 (기록 개수는 2개 이상, 독후감 생성 횟수는 5회 이하)
        user.increaseRecordReviewCount();
        if(records.size() < 2) {
            throw new BusinessException(ErrorCode.RECORD_REVIEW_NOT_ENOUGH_RECORDS,
                    new IllegalArgumentException("현재 기록 개수: " + records.size()));
        }

        // 3. 독후감 생성
        String reviewContent = geminiQueryPort.generateRecordReview(user, records, book, MIN_REVIEW_LENGTH, MAX_REVIEW_LENGTH);

        // 4. 독후감 생성 횟수 갱신
        userCommandPort.update(user);

        return new RecordReviewCreateResult(reviewContent, user.getRecordReviewCount());
    }
}
