package konkuk.thip.user.application.port.out;

import konkuk.thip.user.application.port.in.dto.UserViewAliasChoiceResult;
import konkuk.thip.user.application.port.out.dto.UserQueryDto;

import java.util.List;
import java.util.Set;

public interface UserQueryPort {
    boolean existsByNickname(String nickname);
    Set<Long> findUserIdsParticipatedInRoomsByBookId(Long bookId);

    UserViewAliasChoiceResult getAllAliasesAndCategories();

    List<UserQueryDto> findUsersByNicknameOrderByAccuracy(String keyword, Long userId, Integer size);
}