package konkuk.thip.roompost.adapter.out.persistence.repository.vote;

import konkuk.thip.common.entity.StatusType;
import konkuk.thip.roompost.adapter.out.jpa.VoteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteJpaRepository extends JpaRepository<VoteJpaEntity, Long>, VoteQueryRepository {
    Optional<VoteJpaEntity> findByPostIdAndStatus(Long postId, StatusType status);
}
