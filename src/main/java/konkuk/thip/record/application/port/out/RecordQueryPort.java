package konkuk.thip.record.application.port.out;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.record.application.port.out.dto.PostQueryDto;

public interface RecordQueryPort {

     CursorBasedList<PostQueryDto> searchMyRecords(Long roomId, Long userId, Cursor cursor);

    CursorBasedList<PostQueryDto> searchGroupRecordsByLatest(Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview);

    CursorBasedList<PostQueryDto> searchGroupRecordsByLike(Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview);

    CursorBasedList<PostQueryDto> searchGroupRecordsByComment(Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview);
}

