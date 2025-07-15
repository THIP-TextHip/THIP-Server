package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.mapper.FollowingMapper;
import konkuk.thip.user.adapter.out.mapper.UserMapper;
import konkuk.thip.user.adapter.out.persistence.repository.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.following.FollowingJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.application.port.out.FollowingCommandPort;
import konkuk.thip.user.domain.Following;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Repository
@RequiredArgsConstructor
public class FollowingCommandPersistenceAdapter implements FollowingCommandPort {

    private final FollowingJpaRepository followingJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final AliasJpaRepository aliasJpaRepository;

    private final FollowingMapper followingMapper;
    private final UserMapper userMapper;

    @Override //ACTIVE, INACTIVE 모두 조회
    public Optional<Following> findByUserIdAndTargetUserId(Long userId, Long targetUserId) {
        Optional<FollowingJpaEntity> followingJpaEntity = followingJpaRepository.findByUserAndTargetUser(userId, targetUserId);
        return followingJpaEntity.map(followingMapper::toDomainEntity);
    }

    @Override
    public void save(Following following, User user) { // insert용
        UserJpaEntity targetUser = userJpaRepository.findById(following.getFollowingUserId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND));

        UserJpaEntity followingUser = updateUserFollowingCount(user);
        followingJpaRepository.save(followingMapper.toJpaEntity(followingUser, targetUser));

    }

    @Override
    public void updateStatus(Following following, User user) { // 상태변경 용
        updateUserFollowingCount(user);

        FollowingJpaEntity entity = followingJpaRepository.findByUserAndTargetUser(following.getFollowerUserId(), following.getFollowingUserId())
                .orElseThrow(() -> new EntityNotFoundException(FOLLOW_NOT_FOUND));

        entity.setStatus(following.getStatus());

    }

    private UserJpaEntity updateUserFollowingCount(User user) {
        AliasJpaEntity aliasJpaEntity = aliasJpaRepository.findByValue(user.getAlias().getValue()).orElseThrow(
                () -> new EntityNotFoundException(ALIAS_NOT_FOUND));
        UserJpaEntity userJpaEntity = userMapper.toJpaEntity(user, aliasJpaEntity);
        userJpaRepository.save(userJpaEntity);
        return userJpaEntity;
    }
}
