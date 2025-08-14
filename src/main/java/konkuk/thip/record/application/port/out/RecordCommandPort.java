package konkuk.thip.record.application.port.out;


import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.record.domain.Record;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.RECORD_NOT_FOUND;

public interface RecordCommandPort {

    Long saveRecord(Record record);

    void update(Record record);

    Optional<Record> findById(Long id);

    default Record getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RECORD_NOT_FOUND));
    }

    void delete(Record record);
}
