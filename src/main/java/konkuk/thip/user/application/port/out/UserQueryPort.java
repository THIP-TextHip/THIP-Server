package konkuk.thip.user.application.port.out;

import konkuk.thip.user.application.port.in.dto.UserViewAliasChoiceResult;

import java.util.Set;

public interface UserQueryPort {
    boolean existsByNickname(String nickname);
    Set<Long> findUserIdsParticipatedInRoomsByBookId(Long bookId);

    UserViewAliasChoiceResult getAllAliasesAndCategories();
}
