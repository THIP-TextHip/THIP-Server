package konkuk.thip.roompost.application.port.in;

import konkuk.thip.roompost.application.port.in.dto.record.RecordDeleteCommand;

public interface RecordDeleteUseCase {
    Long deleteRecord(RecordDeleteCommand command);
}
