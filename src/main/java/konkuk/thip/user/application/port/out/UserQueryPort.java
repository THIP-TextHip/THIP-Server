package konkuk.thip.user.application.port.out;

import java.util.Set;

public interface UserQueryPort {
    boolean existsByNickname(String nickname);
    Set<Long> findUserIdsParticipatedInRoomsByBookId(Long bookId);
}
