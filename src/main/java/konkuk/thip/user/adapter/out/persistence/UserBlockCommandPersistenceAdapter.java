package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.user.adapter.out.jpa.UserBlockJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.mapper.UserBlockMapper;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.block.UserBlockJpaRepository;
import konkuk.thip.user.application.port.out.UserBlockCommandPort;
import konkuk.thip.user.domain.UserBlock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.BLOCK_NOT_FOUND;
import static konkuk.thip.common.exception.code.ErrorCode.USER_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class UserBlockCommandPersistenceAdapter implements UserBlockCommandPort {

    private final UserBlockJpaRepository userBlockJpaRepository;
    private final UserJpaRepository userJpaRepository;

    private final UserBlockMapper userBlockMapper;

    @Override
    public Optional<UserBlock> findByUserIdAndTargetUserId(Long userId, Long targetUserId) {
        return userBlockJpaRepository.findByUserAndBlockedUser(userId, targetUserId)
                .map(userBlockMapper::toDomainEntity);
    }

    @Override
    public void save(UserBlock userBlock) {
        UserJpaEntity userJpaEntity = userJpaRepository.findByUserId(userBlock.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
        UserJpaEntity blockedUserJpaEntity = userJpaRepository.findByUserId(userBlock.getBlockedUserId())
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        userBlockJpaRepository.save(userBlockMapper.toJpaEntity(userJpaEntity, blockedUserJpaEntity));
    }

    @Override
    public void deleteBlock(UserBlock userBlock) {
        UserBlockJpaEntity userBlockJpaEntity = userBlockJpaRepository
                .findByUserAndBlockedUser(userBlock.getUserId(), userBlock.getBlockedUserId())
                .orElseThrow(() -> new EntityNotFoundException(BLOCK_NOT_FOUND));

        userBlockJpaRepository.delete(userBlockJpaEntity);
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        userBlockJpaRepository.deleteAllByUserIdOrBlockedUserId(userId);
    }
}
