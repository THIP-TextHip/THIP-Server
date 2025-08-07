package konkuk.thip.record.application.service;

import jakarta.transaction.Transactional;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.record.application.port.in.RecordCreateUseCase;
import konkuk.thip.record.application.port.in.dto.RecordCreateCommand;
import konkuk.thip.record.application.port.in.dto.RecordCreateResult;
import konkuk.thip.record.application.port.out.RecordCommandPort;
import konkuk.thip.record.domain.Record;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.application.service.validator.RoomParticipantValidator;
import konkuk.thip.room.domain.Room;
import konkuk.thip.room.domain.RoomParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static konkuk.thip.common.exception.code.ErrorCode.RECORD_CANNOT_BE_OVERVIEW;

@Service
@RequiredArgsConstructor
public class RecordCreateService implements RecordCreateUseCase {

    private final RecordCommandPort recordCommandPort;
    private final RoomCommandPort roomCommandPort;
    private final BookCommandPort bookCommandPort;
    private final RoomParticipantCommandPort roomParticipantCommandPort;

    private final RoomParticipantValidator roomParticipantValidator;

    @Transactional
    @Override
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

        // 2. UserRoom, Room, Book 조회
        RoomParticipant roomParticipant = roomParticipantCommandPort.getByUserIdAndRoomIdOrThrow(command.userId(), command.roomId());
        Room room = roomCommandPort.getByIdOrThrow(record.getRoomId());
        Book book = bookCommandPort.findById(room.getBookId());

        // 3. 유효성 검증
        validateRoom(room);
        validateRoomParticipant(roomParticipant, command.isOverview());
        validateRecord(record, book);

        // 4. UserRoom의 currentPage, userPercentage 업데이트
        updateRoomProgress(roomParticipant, record, book, room);

        // 5. Record 저장
        Long newRecordId = recordCommandPort.saveRecord(record);

        return RecordCreateResult.of(newRecordId, command.roomId());
    }

    private void updateRoomProgress(RoomParticipant roomParticipant, Record record, Book book, Room room) {
        if(roomParticipant.updateUserProgress(record.getPage(), book.getPageCount())) {
            // userPercentage가 업데이트되었으면 Room의 roomPercentage 업데이트
            List<RoomParticipant> roomParticipantList = roomParticipantCommandPort.findAllByRoomId(record.getRoomId());
            Double totalUserPercentage = roomParticipantList.stream()
                    .map(RoomParticipant::getUserPercentage)
                    .reduce(0.0, Double::sum);
            room.updateRoomPercentage(totalUserPercentage / roomParticipantList.size());
        }
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
