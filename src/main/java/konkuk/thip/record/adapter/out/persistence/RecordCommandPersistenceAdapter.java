package konkuk.thip.record.adapter.out.persistence;

import konkuk.thip.record.adapter.out.mapper.RecordMapper;
import konkuk.thip.record.application.port.out.RecordCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecordCommandPersistenceAdapter implements RecordCommandPort {

    private final RecordJpaRepository recordJpaRepository;
    private final RecordMapper recordMapper;

}
