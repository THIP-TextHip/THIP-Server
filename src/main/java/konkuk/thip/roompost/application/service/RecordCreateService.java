package konkuk.thip.roompost.application.service;

import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.roompost.application.port.in.RecordCreateUseCase;
import konkuk.thip.roompost.application.port.in.dto.record.RecordCreateCommand;
import konkuk.thip.roompost.application.port.in.dto.record.RecordCreateResult;
import konkuk.thip.roompost.application.port.out.RecordCommandPort;
import konkuk.thip.roompost.domain.Record;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import konkuk.thip.room.domain.Room;
import konkuk.thip.room.domain.RoomParticipant;
import konkuk.thip.roompost.application.service.manager.RoomProgressManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static konkuk.thip.common.exception.code.ErrorCode.RECORD_CANNOT_BE_OVERVIEW;

@Service
@RequiredArgsConstructor
public class RecordCreateService implements RecordCreateUseCase {

    private final RecordCommandPort recordCommandPort;
    private final RoomCommandPort roomCommandPort;
    private final BookCommandPort bookCommandPort;
    private final RoomParticipantCommandPort roomParticipantCommandPort;

    private final RoomParticipantValidator roomParticipantValidator;
    private final RoomProgressManager roomProgressManager;

    @Override
    @Transactional
    //todo updateRoomPercentage 스케줄러로 책임을 분리할지 논의
    public RecordCreateResult createRecord(RecordCreateCommand command) {
        roomParticipantValidator.validateUserIsRoomMember(command.roomId(), command.userId());

        // 1. Record 생성
        Record record = Record.withoutId(
                command.content(),
                command.userId(),
                command.page(),
                command.isOverview(),
                command.roomId()
        );

        // 2. RoomParticipant, Room, Book 조회
        RoomParticipant roomParticipant = roomParticipantCommandPort.getByUserIdAndRoomIdOrThrow(command.userId(), command.roomId());
        Room room = roomCommandPort.getByIdOrThrow(record.getRoomId());
        Book book = bookCommandPort.findById(room.getBookId());

        // 3. 유효성 검증
        validateRoom(room);
        validateRoomParticipant(roomParticipant, command.isOverview());
        validateRecord(record, book);

        // 4. 문제없는 경우 Record 저장
        Long newRecordId = recordCommandPort.saveRecord(record);

        // 5. RoomParticipant, Room progress 정보 update
        roomProgressManager.updateUserAndRoomProgress(roomParticipant, room, book, record.getPage());

        return RecordCreateResult.of(newRecordId, command.roomId());
    }

    private void validateRoomParticipant(RoomParticipant roomParticipant, boolean isOverview) {
        // UserRoom의 총평 작성 가능 여부 검증
        if (!roomParticipant.canWriteOverview() && isOverview) {
            String message = String.format(
                    "총평(isOverview)은 사용자 진행률이 80%% 이상일 때만 가능합니다. 현재 사용자 진행률 = %.2f%%",
                    roomParticipant.getUserPercentage()
            );
            throw new BusinessException(RECORD_CANNOT_BE_OVERVIEW, new IllegalStateException(message));
        }
    }

    private void validateRoom(Room room) {
        // 방이 만료되었는지 검증
        room.validateRoomExpired();
    }

    private void validateRecord(Record record, Book book) {
        // 페이지 유효성 검증
        record.validatePage(book.getPageCount());

        // 총평 유효성 검증
        record.validateOverview(book.getPageCount());
    }
}
