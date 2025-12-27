package konkuk.thip.outbox.application.port.out;

import konkuk.thip.outbox.adapter.out.jpa.OutboxEventJpaEntity;
import konkuk.thip.outbox.adapter.out.jpa.OutboxStatus;

import java.util.List;

public interface OutboxEventPersistencePort {
    void save(OutboxEventJpaEntity entity);

    List<OutboxEventJpaEntity> findTop1000ByStatusOrderByIdAsc(OutboxStatus pending);
}
