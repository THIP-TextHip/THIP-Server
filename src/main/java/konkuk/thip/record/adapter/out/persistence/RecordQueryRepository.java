package konkuk.thip.record.adapter.out.persistence;

import konkuk.thip.record.adapter.in.web.response.RecordSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RecordQueryRepository {

    Page<RecordSearchResponse.RecordSearchResult> findRecordsByRoom(Long roomId, String viewType, Integer pageStart, Integer pageEnd, Boolean isOverview, Long userId, Pageable pageable);

}
