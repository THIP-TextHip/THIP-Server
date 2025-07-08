package konkuk.thip.record.application.service;

import jakarta.transaction.Transactional;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.record.application.port.in.RecordCreateUseCase;
import konkuk.thip.record.application.port.in.dto.RecordCreateCommand;
import konkuk.thip.record.application.port.out.RecordCommandPort;
import konkuk.thip.record.domain.Record;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.domain.Room;
import konkuk.thip.user.application.port.out.UserRoomCommandPort;
import konkuk.thip.user.domain.UserRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class RecordCreateService implements RecordCreateUseCase {

    private final RecordCommandPort recordCommandPort;
    private final RoomCommandPort roomCommandPort;
    private final BookCommandPort bookCommandPort;
    private final UserRoomCommandPort userRoomCommandPort;

    @Transactional
    @Override
    //todo updateRoomPercentage 스케줄러로 책임을 분리할지 논의
    public Long createRecord(RecordCreateCommand command) {
        // 1. Record 생성
        Record record = Record.withoutId(
                command.content(),
                command.userId(),
                command.page(),
                command.isOverview(),
                command.roomId()
        );

        // 2. UserRoom, Room, Book 조회
        UserRoom userRoom = userRoomCommandPort.findByUserIdAndRoomId(command.userId(), command.roomId());
        Room room = roomCommandPort.findById(record.getRoomId());
        Book book = bookCommandPort.findById(room.getBookId());

        // 3. 유효성 검증
        validateRoom(room);
        validateUserRoom(userRoom);
        validateRecord(record, book);

        // 4. UserRoom의 currentPage, userPercentage 업데이트
        updateRoomProgress(userRoom, record, book, room);

        // 5. Record 저장
        return recordCommandPort.saveRecord(record);
    }

    private void updateRoomProgress(UserRoom userRoom, Record record, Book book, Room room) {
        if(userRoom.updateUserProgress(record.getPage(), book.getPageCount())) {
            // userPercentage가 업데이트되었으면 Room의 roomPercentage 업데이트
            List<UserRoom> userRoomList = userRoomCommandPort.findAllByRoomId(record.getRoomId());
            Double totalUserPercentage = userRoomList.stream()
                    .map(UserRoom::getUserPercentage)
                    .reduce(0.0, Double::sum);
            room.updateRoomPercentage(totalUserPercentage / userRoomList.size());
        }
    }

    private void validateUserRoom(UserRoom userRoom) {
        // UserRoom의 총평 작성 가능 여부 검증
        if (!userRoom.canWriteOverview()) {
            String message = String.format(
                    "총평(isOverview)은 사용자 진행률이 80%% 이상일 때만 가능합니다. 현재 사용자 진행률 = %.2f%%",
                    userRoom.getUserPercentage()
            );
            throw new InvalidStateException(RECORD_CANNOT_BE_OVERVIEW, new IllegalStateException(message));
        }
    }

    private void validateRoom(Room room) {
        // 방이 만료되었는지 검증
        if (room.isExpired()) {
            throw new BusinessException(RECORD_CANNOT_WRITE_IN_EXPIRED_ROOM);
        }
    }

    private void validateRecord(Record record, Book book) {
        // 페이지 유효성 검증
        record.validatePage(book.getPageCount());

        // 총평 유효성 검증
        record.validateOverview(book.getPageCount());
    }
}
