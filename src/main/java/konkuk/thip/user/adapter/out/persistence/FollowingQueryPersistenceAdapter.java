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
        LocalDateTime cursorVal = cursor != null && !cursor.isBlank() ? DateUtil.parseDateTime(cursor) : null;
        List<FollowQueryDto> followerDtos = followingJpaRepository.findFollowerDtosByUserIdBeforeCreatedAt(
                userId,
                cursorVal,
                size
        );

        return CursorBasedList.of(followerDtos, size, followerDto -> followerDto.createdAt().toString());
    }

    @Override
    public CursorBasedList<FollowQueryDto> getFollowingByUserId(Long userId, String cursor, int size) {
        LocalDateTime cursorVal = cursor != null && !cursor.isBlank() ? DateUtil.parseDateTime(cursor) : null;
        List<FollowQueryDto> followingDtos = followingJpaRepository.findFollowingDtosByUserIdBeforeCreatedAt(
                userId,
                cursorVal,
                size
        );

        return CursorBasedList.of(followingDtos, size, followingDto -> followingDto.createdAt().toString());
    }
}
