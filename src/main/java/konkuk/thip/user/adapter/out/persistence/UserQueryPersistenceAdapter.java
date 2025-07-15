package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.user.adapter.out.mapper.UserMapper;
import konkuk.thip.user.adapter.out.persistence.repository.AliasJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.application.port.in.dto.UserViewAliasChoiceResult;
import konkuk.thip.user.application.port.out.UserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@RequiredArgsConstructor
public class UserQueryPersistenceAdapter implements UserQueryPort {

    private final UserJpaRepository userJpaRepository;
    private final AliasJpaRepository aliasJpaRepository;
    private final UserMapper userMapper;

    @Override
    public boolean existsByNickname(String nickname) {
        return userJpaRepository.existsByNickname(nickname);
    }

    @Override
    public Set<Long> findUserIdsParticipatedInRoomsByBookId(Long bookId) {
        return  userJpaRepository.findUserIdsByBookId(bookId);
    }

    @Override
    public UserViewAliasChoiceResult getAllAliasesAndCategories() {
        return aliasJpaRepository.getAllAliasesAndCategories();
    }
}