package konkuk.thip.record.adapter.out.persistence;

import konkuk.thip.record.adapter.in.web.response.RecordSearchResponse;
import konkuk.thip.record.adapter.out.persistence.repository.RecordJpaRepository;
import konkuk.thip.record.application.port.out.RecordQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecordQueryPersistenceAdapter implements RecordQueryPort {

    private final RecordJpaRepository recordJpaRepository;

    @Override
    public Page<RecordSearchResponse.RecordSearchResult> findRecordsByRoom(Long roomId, String type, Integer pageStart, Integer pageEnd, Boolean isOverview, Long userId, Pageable pageable) {
        return recordJpaRepository.findRecordsByRoom(roomId, type, pageStart, pageEnd, isOverview, userId, pageable);
    }
}

