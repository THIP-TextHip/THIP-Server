package konkuk.thip.record.adapter.out.persistence.repository;

import konkuk.thip.record.adapter.out.jpa.RecordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordJpaRepository extends JpaRepository<RecordJpaEntity, Long>, RecordQueryRepository {

}
