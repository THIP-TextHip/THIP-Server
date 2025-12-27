package konkuk.thip.outbox.adapter.out.persistence.repository;

import konkuk.thip.outbox.adapter.out.jpa.OutboxEventJpaEntity;
import konkuk.thip.outbox.adapter.out.jpa.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, Long> {
    List<OutboxEventJpaEntity> findTop1000ByOutboxStatusOrderByIdAsc(OutboxStatus pending);
}
