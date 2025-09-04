package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.mapper.FollowingMapper;
import konkuk.thip.user.adapter.out.mapper.UserMapper;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.following.FollowingJpaRepository;
import konkuk.thip.user.application.port.out.FollowingCommandPort;
import konkuk.thip.user.domain.Following;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;

import static konkuk.thip.common.exception.code.ErrorCode.FOLLOW_NOT_FOUND;
import static konkuk.thip.common.exception.code.ErrorCode.USER_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class FollowingCommandPersistenceAdapter implements FollowingCommandPort {

    private final FollowingJpaRepository followingJpaRepository;
    private final UserJpaRepository userJpaRepository;

    private final FollowingMapper followingMapper;
    private final UserMapper userMapper;

    @Override //ACTIVE만 조회
    public Optional<Following> findByUserIdAndTargetUserId(Long userId, Long targetUserId) {
        Optional<FollowingJpaEntity> followingJpaEntity = followingJpaRepository.findByUserAndTargetUser(userId, targetUserId);
        return followingJpaEntity.map(followingMapper::toDomainEntity);
    }

    @Override
    public void save(Following following, User targetUser) { // insert용
        UserJpaEntity userJpaEntity = userJpaRepository.findByUserId(following.getUserId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND));

        UserJpaEntity targetUserJpaEntity = updateUserFollowerCount(targetUser);
        followingJpaRepository.save(followingMapper.toJpaEntity(userJpaEntity, targetUserJpaEntity));

    }

    @Override
    public void deleteFollowing(Following following, User targetUser) {
        updateUserFollowerCount(targetUser);

        FollowingJpaEntity followingJpaEntity = followingJpaRepository.findByUserAndTargetUser(following.getUserId(), following.getFollowingUserId())
                .orElseThrow(() -> new EntityNotFoundException(FOLLOW_NOT_FOUND));

        followingJpaRepository.delete(followingJpaEntity);
    }

    @Override
    public void deleteAllByUserId(Long userId) {

        // 1. 탈퇴 유저가 팔로우 중인 유저들 ID 조회
        List<Long> targetUserIds = followingJpaRepository.findAllTargetUserIdsByUserId(userId);
        // 2. 탈퇴한 유저의 모든 팔로잉 관계 삭제
        followingJpaRepository.deleteAllByUserIdOrFollowingUserId(userId);
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            return; //early return
        }
        // 3. 해당 ID들로 JPA 엔티티 직접 조회
        List<UserJpaEntity> userEntities = userJpaRepository.findAllById(targetUserIds);
        // 4. 엔티티에서 직접 팔로워 수 감소
        userEntities.forEach(entity ->
                entity.setFollowerCount(Math.max(0, entity.getFollowerCount() - 1)));
        userJpaRepository.saveAll(userEntities);
    }


    private UserJpaEntity updateUserFollowerCount(User targetUser) {
        UserJpaEntity userJpaEntity = userJpaRepository.findByUserId(targetUser.getId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND)
        );

        userJpaEntity.updateFrom(targetUser);
        userJpaRepository.save(userJpaEntity);
        return userJpaEntity;
    }
}
