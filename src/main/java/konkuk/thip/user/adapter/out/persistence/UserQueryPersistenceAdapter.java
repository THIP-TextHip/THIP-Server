package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.user.adapter.out.mapper.UserMapper;
import konkuk.thip.user.application.port.out.UserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserQueryPersistenceAdapter implements UserQueryPort {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    @Override
    public boolean existsByNickname(String nickname) {
        return userJpaRepository.existsByNickname(nickname);
    }
}
