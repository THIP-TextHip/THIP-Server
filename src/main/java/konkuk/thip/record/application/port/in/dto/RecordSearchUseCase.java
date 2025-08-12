package konkuk.thip.record.application.port.in.dto;

import konkuk.thip.record.adapter.in.web.response.RecordSearchResponse;

public interface RecordSearchUseCase {

    RecordSearchResponse search(RecordSearchQuery recordSearchQuery);
}
