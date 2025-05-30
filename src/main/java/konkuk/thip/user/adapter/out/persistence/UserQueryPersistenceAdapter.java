package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.mapper.UserMapper;
import konkuk.thip.user.application.port.out.UserQueryPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserQueryPersistenceAdapter implements UserQueryPort {

    private final UserJpaRepository jpaRepository;
    private final UserMapper userMapper;

    @Override
    public User findById(Long id) {
        Optional<UserJpaEntity> byId = jpaRepository.findById(id);

        if (!byId.isPresent()) {
            // 예외 throw -> ex) UserNotFound -> 클라이언트에게 4xx error code throw
        }

        return userMapper.toDomainEntity(byId.get());
    }
}
