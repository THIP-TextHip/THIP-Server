package konkuk.thip.user.adapter.out.persistence.repository.following;

import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import konkuk.thip.user.application.port.out.dto.FollowQueryDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FollowingQueryRepository {
    Optional<FollowingJpaEntity> findByUserAndTargetUser(Long userId, Long targetUserId);

    List<FollowQueryDto> findFollowerDtosByUserIdBeforeCreatedAt(Long userId, LocalDateTime cursor, int size);
}
