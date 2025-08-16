package konkuk.thip.roompost.application.port.in;

import konkuk.thip.roompost.application.port.in.dto.record.RecordCreateCommand;
import konkuk.thip.roompost.application.port.in.dto.record.RecordCreateResult;

public interface RecordCreateUseCase {

    RecordCreateResult createRecord(RecordCreateCommand command);

}
