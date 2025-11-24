package konkuk.thip.roompost.application.port.out;


import java.util.List;
import java.util.Map;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.roompost.domain.Record;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.RECORD_NOT_FOUND;

public interface RecordCommandPort {

    Long save(Record record);

    void update(Record record);

    Optional<Record> findById(Long id);
    List<Long> findByIds(List<Long> ids);

    default Record getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RECORD_NOT_FOUND));
    }

    void delete(Record record);

    void deleteAllByUserId(Long userId);

    void batchUpdateLikeCounts(Map<Long, Integer> idToLikeCount);
}
