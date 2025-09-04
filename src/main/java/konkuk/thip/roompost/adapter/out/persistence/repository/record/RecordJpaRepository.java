package konkuk.thip.roompost.adapter.out.persistence.repository.record;

import konkuk.thip.roompost.adapter.out.jpa.RecordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecordJpaRepository extends JpaRepository<RecordJpaEntity, Long>, RecordQueryRepository {

    /**
     * 소프트 딜리트 적용 대상 entity 단건 조회 메서드
     */
    Optional<RecordJpaEntity> findByPostId(Long postId);

    @Query("SELECT r.postId FROM RecordJpaEntity r WHERE r.userJpaEntity.userId = :userId")
    List<Long> findRecordIdsByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RecordJpaEntity r SET r.status = 'INACTIVE' WHERE r.userJpaEntity.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
