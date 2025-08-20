package konkuk.thip.user.adapter.out.persistence.repository.following;

import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.application.port.out.dto.FollowingQueryDto;
import konkuk.thip.user.application.port.out.dto.UserQueryDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FollowingQueryRepository {
    Optional<FollowingJpaEntity> findByUserAndTargetUser(Long userId, Long targetUserId);

    List<UserQueryDto> findFollowerDtosByUserIdBeforeCreatedAt(Long userId, LocalDateTime cursor, int size);
    List<UserQueryDto> findFollowingDtosByUserIdBeforeCreatedAt(Long userId, LocalDateTime cursor, int size);

    List<UserJpaEntity> findLatestFollowers(Long userId, int size);

    List<FollowingQueryDto> findAllFollowingUsersOrderByFollowedAtDesc(Long userId);
}
