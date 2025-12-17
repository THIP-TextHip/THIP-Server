package konkuk.thip.roompost.application.port.out;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.roompost.application.port.out.dto.RoomPostQueryDto;
import konkuk.thip.roompost.domain.Record;

import java.util.List;

public interface RecordQueryPort {

     CursorBasedList<RoomPostQueryDto> searchMyRecords(Long roomId, Long userId, Cursor cursor);

    CursorBasedList<RoomPostQueryDto> searchGroupRecordsByLatest(Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview);

    CursorBasedList<RoomPostQueryDto> searchGroupRecordsByLike(Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview);

    CursorBasedList<RoomPostQueryDto> searchGroupRecordsByComment(Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview);

    List<Record> findAllByRoomIdAndUserId(Long roomId, Long userId);

    Integer countAllByRoomIdAndUserId(Long roomId, Long userId);
}

