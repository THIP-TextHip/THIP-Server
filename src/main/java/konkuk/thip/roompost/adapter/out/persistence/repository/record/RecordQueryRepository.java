package konkuk.thip.roompost.adapter.out.persistence.repository.record;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.roompost.adapter.out.persistence.RoomPostSortType;
import konkuk.thip.roompost.application.port.out.dto.RoomPostQueryDto;

import java.util.List;

public interface RecordQueryRepository {

    List<RoomPostQueryDto> findMyRecords(Long roomId, Long userId, Cursor cursor);

    List<RoomPostQueryDto> findGroupRecordsOrderBySortType(Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview, RoomPostSortType roomPostSortType);
}
