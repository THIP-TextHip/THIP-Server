package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.common.util.DateUtil;
import konkuk.thip.user.application.port.out.dto.FollowQueryDto;
import konkuk.thip.user.adapter.out.persistence.repository.following.FollowingJpaRepository;
import konkuk.thip.user.application.port.out.FollowingQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FollowingQueryPersistenceAdapter implements FollowingQueryPort {

    private final FollowingJpaRepository followingJpaRepository;

    @Override
    public CursorBasedList<FollowQueryDto> getFollowersByUserId(Long userId, String cursor, int size) {
        LocalDateTime cursorVal = null;
        if (cursor != null && !cursor.isBlank()) {
            cursorVal = DateUtil.parseDateTime(cursor);
        }
        List<FollowQueryDto> dtos = followingJpaRepository.findFollowerDtosByUserIdBeforeCreatedAt(
                userId,
                cursorVal,
                size
        );

        boolean hasNext = dtos.size() > size;
        List<FollowQueryDto> content = hasNext ? dtos.subList(0, size) : dtos;
        String  nextCursor = hasNext ? content.get(size - 1).createdAt().toString() : null;

        return CursorBasedList.of(content, nextCursor);
    }
}
