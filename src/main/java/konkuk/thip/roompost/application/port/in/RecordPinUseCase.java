package konkuk.thip.roompost.application.port.in;

import konkuk.thip.book.application.port.in.dto.BookPinResult;
import konkuk.thip.roompost.application.port.in.dto.record.RecordPinQuery;

public interface RecordPinUseCase {
    BookPinResult pinRecord(RecordPinQuery query);
}
