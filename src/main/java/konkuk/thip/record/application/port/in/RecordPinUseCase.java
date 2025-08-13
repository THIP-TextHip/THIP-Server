package konkuk.thip.record.application.port.in;

import konkuk.thip.record.adapter.in.web.response.RecordPinResponse;
import konkuk.thip.record.application.port.in.dto.RecordPinQuery;

public interface RecordPinUseCase {
    RecordPinResponse pinRecord(RecordPinQuery query);
}
