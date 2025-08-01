package konkuk.thip.record.application.port.in;

import konkuk.thip.record.application.port.in.dto.RecordCreateCommand;
import konkuk.thip.record.application.port.in.dto.RecordCreateResult;

public interface RecordCreateUseCase {

    RecordCreateResult createRecord(RecordCreateCommand command);

}
