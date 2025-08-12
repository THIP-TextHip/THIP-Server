package konkuk.thip.record.adapter.out.persistence.repository;

import konkuk.thip.common.entity.StatusType;
import konkuk.thip.record.adapter.out.jpa.RecordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecordJpaRepository extends JpaRepository<RecordJpaEntity, Long>, RecordQueryRepository {
    Optional<RecordJpaEntity> findByPostIdAndStatus(Long postId, StatusType status);
}
