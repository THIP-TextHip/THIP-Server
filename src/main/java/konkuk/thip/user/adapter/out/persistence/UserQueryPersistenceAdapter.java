package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import konkuk.thip.user.application.port.in.dto.UserViewAliasChoiceResult;
import konkuk.thip.user.application.port.out.UserQueryPort;
import konkuk.thip.user.application.port.out.dto.UserQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class UserQueryPersistenceAdapter implements UserQueryPort {

    private final UserJpaRepository userJpaRepository;
    private final AliasJpaRepository aliasJpaRepository;

    @Override
    public boolean existsByNickname(String nickname) {
        return userJpaRepository.existsByNickname(nickname);
    }

    @Override
    public boolean existsByNicknameAndUserIdNot(String nickname, Long userId) {
        return userJpaRepository.existsByNicknameAndUserIdNot(nickname, userId);
    }

    @Override
    public Set<Long> findUserIdsParticipatedInRoomsByBookId(Long bookId) {
        return  userJpaRepository.findUserIdsByBookId(bookId);
    }

    @Override
    public UserViewAliasChoiceResult getAllAliasesAndCategories() {
        return aliasJpaRepository.getAllAliasesAndCategories();
    }

    @Override
    public List<UserQueryDto> findUsersByNicknameOrderByAccuracy(String keyword, Long userId, Integer size) {
        return userJpaRepository.findUsersByNicknameOrderByAccuracy(keyword, userId, size);
    }
}