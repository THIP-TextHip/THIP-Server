package konkuk.thip.roompost.adapter.out.persistence.repository.record;

import konkuk.thip.roompost.adapter.out.jpa.RecordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecordJpaRepository extends JpaRepository<RecordJpaEntity, Long>, RecordQueryRepository {

    /**
     * 소프트 딜리트 적용 대상 entity 단건 조회 메서드
     */
    Optional<RecordJpaEntity> findByPostId(Long postId);
}
