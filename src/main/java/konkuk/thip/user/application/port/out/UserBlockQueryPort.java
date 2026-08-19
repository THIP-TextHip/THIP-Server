package konkuk.thip.user.application.port.out;

import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.user.application.port.out.dto.BlockedUserQueryDto;

import java.util.Set;

public interface UserBlockQueryPort {

    CursorBasedList<BlockedUserQueryDto> getBlockedUsersByUserId(Long userId, String cursor, int size);

    int getBlockedUserCountByUserId(Long userId);

    boolean existsBlockBetween(Long userId, Long targetUserId);

    // QueryDSL 로 필터링할 수 없는 조회에서 사용한다
    Set<Long> findBlockedUserIdsBothWays(Long userId);
}
