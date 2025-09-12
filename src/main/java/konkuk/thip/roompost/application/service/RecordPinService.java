package konkuk.thip.roompost.application.service;

import konkuk.thip.book.application.mapper.BookQueryMapper;
import konkuk.thip.book.application.port.in.dto.BookPinResult;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.roompost.application.port.in.RecordPinUseCase;
import konkuk.thip.roompost.application.port.in.dto.record.RecordPinQuery;
import konkuk.thip.roompost.application.port.out.RecordCommandPort;
import konkuk.thip.roompost.domain.Record;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecordPinService implements RecordPinUseCase {

    private final RecordCommandPort recordCommandPort;
    private final BookCommandPort bookCommandPort;

    private final RoomParticipantValidator roomParticipantValidator;
    private final BookQueryMapper bookQueryMapper;

    @Override
    @Transactional(readOnly = true)
    public BookPinResult pinRecord(RecordPinQuery query) {

        // 1. 방 참여자 검증
        roomParticipantValidator.validateUserIsRoomMember(query.roomId(), query.userId());

        // 2. 기록 조회 및 검증
        Record record = recordCommandPort.getByIdOrThrow(query.recordId());
        // 2-1. 기록 핀 권한 검증
        record.validatePin(query.userId(),query.roomId());

        // 3. 책 정보 조회 및 반환
        Book book = bookCommandPort.findBookByRoomId(query.roomId());
        return bookQueryMapper.toBookPinResult(book);
    }
}
