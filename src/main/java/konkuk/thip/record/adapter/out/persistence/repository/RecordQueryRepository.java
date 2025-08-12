package konkuk.thip.record.adapter.out.persistence.repository;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.record.adapter.out.persistence.constants.SortType;
import konkuk.thip.record.application.port.out.dto.PostQueryDto;

import java.util.List;

public interface RecordQueryRepository {

    List<PostQueryDto> findMyRecords(Long roomId, Long userId, Cursor cursor);

    List<PostQueryDto> findGroupRecordsOrderBySortType(Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview, SortType sortType);
}
