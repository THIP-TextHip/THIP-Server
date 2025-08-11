package konkuk.thip.vote.adapter.out.persistence.repository;

import konkuk.thip.common.entity.StatusType;
import konkuk.thip.vote.adapter.out.jpa.VoteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteJpaRepository extends JpaRepository<VoteJpaEntity, Long>, VoteQueryRepository {
    Optional<VoteJpaEntity> findByPostIdAndStatus(Long postId, StatusType status);
}
