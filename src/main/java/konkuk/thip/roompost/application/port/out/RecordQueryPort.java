package konkuk.thip.roompost.application.port.out;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.roompost.application.port.out.dto.RoomPostQueryDto;

public interface RecordQueryPort {

     CursorBasedList<RoomPostQueryDto> searchMyRecords(Long roomId, Long userId, Cursor cursor);

    CursorBasedList<RoomPostQueryDto> searchGroupRecordsByLatest(Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview);

    CursorBasedList<RoomPostQueryDto> searchGroupRecordsByLike(Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview);

    CursorBasedList<RoomPostQueryDto> searchGroupRecordsByComment(Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview);
}

