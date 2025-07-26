package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.mapper.FollowingMapper;
import konkuk.thip.user.adapter.out.mapper.UserMapper;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.following.FollowingJpaRepository;
import konkuk.thip.user.application.port.out.FollowingCommandPort;
import konkuk.thip.user.domain.Following;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.USER_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class FollowingCommandPersistenceAdapter implements FollowingCommandPort {

    private final FollowingJpaRepository followingJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final AliasJpaRepository aliasJpaRepository;

    private final FollowingMapper followingMapper;
    private final UserMapper userMapper;

    @Override //ACTIVE만 조회
    public Optional<Following> findByUserIdAndTargetUserId(Long userId, Long targetUserId) {
        Optional<FollowingJpaEntity> followingJpaEntity = followingJpaRepository.findByUserAndTargetUser(userId, targetUserId);
        return followingJpaEntity.map(followingMapper::toDomainEntity);
    }

    @Override
    public void save(Following following, User targetUser) { // insert용
        UserJpaEntity userJpaEntity = userJpaRepository.findById(following.getUserId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND));

        UserJpaEntity targetUserJpaEntity = updateUserFollowerCount(targetUser);
        followingJpaRepository.save(followingMapper.toJpaEntity(userJpaEntity, targetUserJpaEntity));

    }

    @Override
    public void deleteFollowing(Following following, User targetUser) {
        updateUserFollowerCount(targetUser);

        FollowingJpaEntity followingJpaEntity = followingJpaRepository.findByUserAndTargetUser(following.getUserId(), following.getFollowingUserId())
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        followingJpaRepository.delete(followingJpaEntity);
    }

    private UserJpaEntity updateUserFollowerCount(User targetUser) {
        UserJpaEntity userJpaEntity = userJpaRepository.findById(targetUser.getId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND)
        );

        userJpaEntity.updateFrom(targetUser);
        userJpaRepository.save(userJpaEntity);
        return userJpaEntity;
    }
}
