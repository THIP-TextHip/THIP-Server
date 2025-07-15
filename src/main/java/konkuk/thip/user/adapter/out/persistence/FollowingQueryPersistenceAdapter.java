package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.common.util.DateUtil;
import konkuk.thip.user.adapter.in.web.response.UserFollowersResponse;
import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.following.FollowingJpaRepository;
import konkuk.thip.user.application.port.out.FollowingQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class FollowingQueryPersistenceAdapter implements FollowingQueryPort {

    private final FollowingJpaRepository followingJpaRepository;

    @Override
    public Map<Long, Integer> countByFollowingUserIds(List<Long> userIds) {
        return followingJpaRepository.countByFollowingUserIds(userIds);
    }

    @Override
    public UserFollowersResponse getFollowersByUserId(Long userId, String cursor, int size) {
        LocalDateTime nextCursor = null;
        if (cursor != null && !cursor.isBlank()) {
             nextCursor = DateUtil.parseDateTime(cursor);
        }

        List<FollowingJpaEntity> followerEntities =
                followingJpaRepository.findFollowersByUserIdBeforeCreatedAt(userId, nextCursor, size);

        List<UserJpaEntity> followers = followerEntities.stream()
                .map(FollowingJpaEntity::getFollowerUserJpaEntity) // 팔로워 사용자
                .toList();

        Map<Long, Integer> followingCountMap = countByFollowingUserIds(
                followers.stream().map(UserJpaEntity::getUserId).toList()
        );

        List<UserFollowersResponse.Follower> followerList = followers.stream()
                .map(follower -> UserFollowersResponse.Follower.of(follower.getUserId(), follower.getNickname(), follower.getAliasForUserJpaEntity().getImageUrl(), follower.getAliasForUserJpaEntity().getValue(), followingCountMap.getOrDefault(follower.getUserId(), 0)))
                .toList();

        boolean isLast = followerEntities.size() < size;
         nextCursor = isLast ? null :
                followerEntities.get(followerEntities.size() - 1).getCreatedAt();

        return UserFollowersResponse.builder()
                .followerList(followerList)
                .size(followerList.size())
                .nextCursor(nextCursor)
                .isFirst(cursor == null) // cursor가 null이면 첫 페이지
                .isLast(isLast)
                .build();
    }
}
