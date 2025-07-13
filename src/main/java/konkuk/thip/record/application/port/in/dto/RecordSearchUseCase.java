package konkuk.thip.record.application.port.in.dto;

import konkuk.thip.record.adapter.in.web.response.RecordSearchResponse;

public interface RecordSearchUseCase {

    RecordSearchResponse search(Long roomId, String type, String sort, Integer pageStart, Integer pageEnd, Boolean isOverview, Integer pageNum, Long userId);
}
