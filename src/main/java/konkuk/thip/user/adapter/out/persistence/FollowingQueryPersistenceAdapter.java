package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.common.util.DateUtil;
import konkuk.thip.user.application.port.out.dto.UserQueryDto;
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
    public CursorBasedList<UserQueryDto> getFollowersByUserId(Long userId, String cursor, int size) {
        LocalDateTime cursorVal = cursor != null && !cursor.isBlank() ? DateUtil.parseDateTime(cursor) : null;
        List<UserQueryDto> followerDtos = followingJpaRepository.findFollowerDtosByUserIdBeforeCreatedAt(
                userId,
                cursorVal,
                size
        );

        return CursorBasedList.of(followerDtos, size, followerDto -> followerDto.createdAt().toString());
    }

    @Override
    public CursorBasedList<UserQueryDto> getFollowingByUserId(Long userId, String cursor, int size) {
        LocalDateTime cursorVal = cursor != null && !cursor.isBlank() ? DateUtil.parseDateTime(cursor) : null;
        List<UserQueryDto> followingDtos = followingJpaRepository.findFollowingDtosByUserIdBeforeCreatedAt(
                userId,
                cursorVal,
                size
        );

        return CursorBasedList.of(followingDtos, size, followingDto -> followingDto.createdAt().toString());
    }

    @Override
    public List<String> getLatestFollowerImageUrls(Long userId, int size) {
        return followingJpaRepository.findLatestFollowerImageUrls(userId, size);
    }

    @Override
    public boolean isFollowingUser(Long userId, Long targetUserId) {
        return followingJpaRepository.existsByUserJpaEntity_UserIdAndFollowingUserJpaEntity_UserId(userId, targetUserId);
    }
}
