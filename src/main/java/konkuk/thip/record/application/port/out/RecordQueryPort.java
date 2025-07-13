package konkuk.thip.record.application.port.out;

import konkuk.thip.record.adapter.in.web.response.RecordSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RecordQueryPort {

     Page<RecordSearchResponse.RecordSearchResult> findRecordsByRoom(Long roomId, String type, Integer pageStart, Integer pageEnd, Boolean isOverview, Long userId, Pageable pageable);

}
