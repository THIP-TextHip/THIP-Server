package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.mapper.UserMapper;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static konkuk.thip.common.exception.code.ErrorCode.USER_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class UserCommandPersistenceAdapter implements UserCommandPort {

    private final UserJpaRepository userJpaRepository;

    private final UserMapper userMapper;

    @Override
    public Long save(User user) {
        UserJpaEntity userJpaEntity = userMapper.toJpaEntity(user);
        return userJpaRepository.save(userJpaEntity).getUserId();
    }

    @Override
    public User findById(Long userId) {
        UserJpaEntity userJpaEntity = userJpaRepository.findByUserId(userId).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND));

        return userMapper.toDomainEntity(userJpaEntity);
    }

    @Override
    public Map<Long, User> findByIds(List<Long> userIds) {
        List<UserJpaEntity> entities = userJpaRepository.findAllById(userIds);  // 내부 구현 메서드가 jpql 기반이므로 필터 적용 대상임을 확인함
        return entities.stream()
                .map(userMapper::toDomainEntity)
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    @Override
    public void update(User user) {
        UserJpaEntity userJpaEntity = userJpaRepository.findByUserId(user.getId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND)
        );

        userJpaEntity.updateIncludeAliasFrom(user);
        userJpaRepository.save(userJpaEntity);
    }

    @Override
    public void delete(User user) {
        UserJpaEntity userJpaEntity = userJpaRepository.findByUserId(user.getId()).orElseThrow(
                () -> new EntityNotFoundException(USER_NOT_FOUND)
        );

        userJpaEntity.softDelete(user);
        userJpaRepository.save(userJpaEntity);
    }
}
