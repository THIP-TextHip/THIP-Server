package konkuk.thip.roompost.application.port.in;

import konkuk.thip.roompost.adapter.in.web.response.RecordPinResponse;
import konkuk.thip.roompost.application.port.in.dto.record.RecordPinQuery;

public interface RecordPinUseCase {
    RecordPinResponse pinRecord(RecordPinQuery query);
}
