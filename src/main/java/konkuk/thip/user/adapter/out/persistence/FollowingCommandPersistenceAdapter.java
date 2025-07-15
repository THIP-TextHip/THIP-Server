package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.mapper.FollowingMapper;
import konkuk.thip.user.adapter.out.persistence.repository.following.FollowingJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.application.port.out.FollowingCommandPort;
import konkuk.thip.user.domain.Following;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.FOLLOW_NOT_FOUND;
import static konkuk.thip.common.exception.code.ErrorCode.USER_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class FollowingCommandPersistenceAdapter implements FollowingCommandPort {

    private final FollowingJpaRepository followingJpaRepository;
    private final UserJpaRepository userJpaRepository;

    private final FollowingMapper followingMapper;

    @Override //ACTIVE, INACTIVE 모두 조회
    public Optional<Following> findByUserIdAndTargetUserId(Long userId, Long targetUserId) {
        Optional<FollowingJpaEntity> followingJpaEntity = followingJpaRepository.findByUserAndTargetUser(userId, targetUserId);
        return followingJpaEntity.map(followingMapper::toDomainEntity);
    }

    @Override
    public void save(Following following) { // insert용
        UserJpaEntity userJpaEntity = userJpaRepository.findById(following.getFollowerUserId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND));

        UserJpaEntity targetUser = userJpaRepository.findById(following.getFollowingUserId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND));

        followingJpaRepository.save(followingMapper.toJpaEntity(userJpaEntity, targetUser));
    }

    @Override
    public void updateStatus(Following following) { // 상태변경 용
        FollowingJpaEntity entity = followingJpaRepository.findByUserAndTargetUser(following.getFollowerUserId(), following.getFollowingUserId())
                .orElseThrow(() -> new EntityNotFoundException(FOLLOW_NOT_FOUND));

        entity.setStatus(following.getStatus());
    }
}
