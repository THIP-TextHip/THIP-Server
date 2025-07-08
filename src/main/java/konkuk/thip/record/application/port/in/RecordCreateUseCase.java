package konkuk.thip.record.application.port.in;

import konkuk.thip.record.application.port.in.dto.RecordCreateCommand;

public interface RecordCreateUseCase {

    Long createRecord(RecordCreateCommand command);

}
