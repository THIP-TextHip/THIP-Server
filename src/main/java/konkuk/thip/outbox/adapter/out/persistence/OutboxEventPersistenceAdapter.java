package konkuk.thip.outbox.adapter.out.persistence;

import konkuk.thip.outbox.adapter.out.jpa.OutboxEventJpaEntity;
import konkuk.thip.outbox.adapter.out.jpa.OutboxStatus;
import konkuk.thip.outbox.adapter.out.persistence.repository.OutboxEventJpaRepository;
import konkuk.thip.outbox.application.port.out.OutboxEventPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OutboxEventPersistenceAdapter implements OutboxEventPersistencePort {

    private final OutboxEventJpaRepository outboxEventJpaRepository;

    @Override
    public void save(OutboxEventJpaEntity entity) {
        outboxEventJpaRepository.save(entity);
    }

    @Override
    public List<OutboxEventJpaEntity> findTop1000ByStatusOrderByIdAsc(OutboxStatus pending) {
        return outboxEventJpaRepository.findTop1000ByOutboxStatusOrderByIdAsc(pending);
    }


}
