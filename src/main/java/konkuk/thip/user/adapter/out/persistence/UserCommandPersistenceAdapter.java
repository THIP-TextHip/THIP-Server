package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.user.adapter.out.mapper.UserMapper;
import konkuk.thip.user.application.port.out.UserCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCommandPersistenceAdapter implements UserCommandPort {

    private final UserJpaRepository jpaRepository;
    private final UserMapper userMapper;

}
