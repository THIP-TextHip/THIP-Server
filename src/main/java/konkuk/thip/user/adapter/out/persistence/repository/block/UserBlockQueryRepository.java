package konkuk.thip.user.adapter.out.persistence.repository.block;

import konkuk.thip.user.adapter.out.jpa.UserBlockJpaEntity;
import konkuk.thip.user.application.port.out.dto.BlockedUserQueryDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserBlockQueryRepository {

    Optional<UserBlockJpaEntity> findByUserAndBlockedUser(Long userId, Long blockedUserId);

    // 해제하려면 대상을 식별해야 하므로 차단 필터를 적용하지 않는다
    List<BlockedUserQueryDto> findBlockedUserDtosByUserId(Long userId, LocalDateTime cursorCreatedAt, Long cursorBlockId, int size);

    // QueryDSL 로 필터링할 수 없는 조회에서 사용한다
    Set<Long> findBlockedUserIdsBothWays(Long userId);
}
