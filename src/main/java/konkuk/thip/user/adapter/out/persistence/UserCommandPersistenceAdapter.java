package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.mapper.UserMapper;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static konkuk.thip.common.entity.StatusType.ACTIVE;
import static konkuk.thip.common.exception.code.ErrorCode.ALIAS_NOT_FOUND;
import static konkuk.thip.common.exception.code.ErrorCode.USER_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class UserCommandPersistenceAdapter implements UserCommandPort {

    private final UserJpaRepository userJpaRepository;
    private final AliasJpaRepository aliasJpaRepository;

    private final UserMapper userMapper;

    @Override
    public Long save(User user) {
        AliasJpaEntity aliasJpaEntity = aliasJpaRepository.findByValue(user.getAlias().getValue()).orElseThrow(
                () -> new EntityNotFoundException(ALIAS_NOT_FOUND));

        UserJpaEntity userJpaEntity = userMapper.toJpaEntity(user, aliasJpaEntity);
        return userJpaRepository.save(userJpaEntity).getUserId();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findByUserIdAndStatus(id, ACTIVE)
                .map(userMapper::toDomainEntity);
    }

    @Override
    public Map<Long, User> findByIds(List<Long> userIds) {
        List<UserJpaEntity> entities = userJpaRepository.findAllByUserIdInAndStatus(userIds,ACTIVE);
        return entities.stream()
                .map(userMapper::toDomainEntity)
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    @Override
    public void update(User user) {
        UserJpaEntity userJpaEntity = userJpaRepository.findById(user.getId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND)
        );

        aliasJpaRepository.findByValue(user.getAlias().getValue()).ifPresentOrElse(
                aliasJpaEntity -> userJpaEntity.updateIncludeAliasFrom(user, aliasJpaEntity),
                () -> {
                    throw new EntityNotFoundException(ALIAS_NOT_FOUND);
                }
        );

        userJpaRepository.save(userJpaEntity);
    }

    @Override
    public void delete(User user) {
        UserJpaEntity userJpaEntity = userJpaRepository.findById(user.getId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND)
        );
        userJpaRepository.delete(userJpaEntity);
    }
}
