package konkuk.thip.record.application.port.in;

import konkuk.thip.record.application.port.in.dto.RecordDeleteCommand;

public interface RecordDeleteUseCase {
    Long deleteRecord(RecordDeleteCommand command);
}
