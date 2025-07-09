package konkuk.thip.record.adapter.out.persistence;

import konkuk.thip.record.adapter.out.jpa.RecordJpaEntity;

import java.util.List;

public interface RecordQueryRepository {

    List<RecordJpaEntity> findRecordsByRoom(Long roomId, String type, Integer pageStart, Integer pageEnd, Long userId);

}
