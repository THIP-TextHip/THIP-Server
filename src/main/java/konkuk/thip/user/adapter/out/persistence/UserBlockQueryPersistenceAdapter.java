package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.user.adapter.out.persistence.repository.block.UserBlockJpaRepository;
import konkuk.thip.user.application.port.out.UserBlockQueryPort;
import konkuk.thip.user.application.port.out.dto.BlockedUserQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class UserBlockQueryPersistenceAdapter implements UserBlockQueryPort {

    private final UserBlockJpaRepository userBlockJpaRepository;

    @Override
    public CursorBasedList<BlockedUserQueryDto> getBlockedUsersByUserId(Long userId, String cursorStr, int size) {
        Cursor cursor = Cursor.from(cursorStr, size);

        LocalDateTime cursorCreatedAt = cursor.isFirstRequest() ? null : cursor.getLocalDateTime(0);
        Long cursorBlockId = cursor.isFirstRequest() ? null : cursor.getLong(1);

        List<BlockedUserQueryDto> blockedUserDtos = userBlockJpaRepository.findBlockedUserDtosByUserId(
                userId, cursorCreatedAt, cursorBlockId, size
        );

        return CursorBasedList.of(blockedUserDtos, size,
                dto -> new Cursor(List.of(dto.createdAt().toString(), dto.blockId().toString())).toEncodedString());
    }

    @Override
    public int getBlockedUserCountByUserId(Long userId) {
        return userBlockJpaRepository.countBlockedUsersByUserId(userId);
    }

    @Override
    public boolean existsBlockBetween(Long userId, Long targetUserId) {
        return userBlockJpaRepository.existsBlockBetween(userId, targetUserId);
    }

    @Override
    public Set<Long> findBlockedUserIdsBothWays(Long userId) {
        return userBlockJpaRepository.findBlockedUserIdsBothWays(userId);
    }
}
