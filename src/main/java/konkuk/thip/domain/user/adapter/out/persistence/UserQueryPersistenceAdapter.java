package konkuk.thip.domain.user.adapter.out.persistence;

import konkuk.thip.domain.user.adapter.out.mapper.UserMapper;
import konkuk.thip.domain.user.application.port.out.UserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserQueryPersistenceAdapter implements UserQueryPort {

    private final UserJpaRepository jpaRepository;
    private final UserMapper userMapper;

}
