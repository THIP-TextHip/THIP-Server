package konkuk.thip.record.application.service;

import jakarta.transaction.Transactional;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.record.application.port.in.RecordCreateUseCase;
import konkuk.thip.record.application.port.in.dto.RecordCreateCommand;
import konkuk.thip.record.application.port.out.RecordCommandPort;
import konkuk.thip.record.domain.Record;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.domain.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecordCreateService implements RecordCreateUseCase {

    private final RecordCommandPort recordCommandPort;
    private final RoomCommandPort roomCommandPort;
    private final BookCommandPort bookCommandPort;

    @Transactional
    @Override
    public Long createRecord(RecordCreateCommand command) {
        Record record = Record.withoutId(
                command.content(),
                command.userId(),
                command.page(),
                command.isOverview(),
                command.roomId()
        );

        validateRecord(record);

        return recordCommandPort.saveRecord(record);
    }

    private void validateRecord(Record record) {
        Room room = roomCommandPort.findById(record.getRoomId());
        Book book = bookCommandPort.findById(room.getBookId());

        // 페이지 유효성 검증
        record.validatePage(book.getPageCount());

        // 총평 유효성 검증
        record.validateOverview(book.getPageCount());
    }
}
