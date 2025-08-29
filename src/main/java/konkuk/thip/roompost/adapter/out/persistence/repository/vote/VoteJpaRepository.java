package konkuk.thip.roompost.adapter.out.persistence.repository.vote;

import konkuk.thip.roompost.adapter.out.jpa.VoteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteJpaRepository extends JpaRepository<VoteJpaEntity, Long>, VoteQueryRepository {

    /**
     * 소프트 딜리트 적용 대상 entity 단건 조회 메서드
     */
    Optional<VoteJpaEntity> findByPostId(Long postId);
}
