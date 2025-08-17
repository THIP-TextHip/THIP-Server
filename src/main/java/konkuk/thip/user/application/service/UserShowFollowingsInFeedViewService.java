package konkuk.thip.user.application.service;

import konkuk.thip.feed.application.port.out.FeedQueryPort;
import konkuk.thip.user.adapter.in.web.response.UserShowFollowingsInFeedViewResponse;
import konkuk.thip.user.application.mapper.UserQueryMapper;
import konkuk.thip.user.application.port.in.UserShowFollowingsInFeedViewUseCase;
import konkuk.thip.user.application.port.out.FollowingQueryPort;
import konkuk.thip.user.application.port.out.dto.FollowingQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserShowFollowingsInFeedViewService implements UserShowFollowingsInFeedViewUseCase {

    private static final int SIZE = 10;
    private final FollowingQueryPort followingQueryPort;
    private final FeedQueryPort feedQueryPort;
    private final UserQueryMapper userQueryMapper;

    @Override
    @Transactional(readOnly = true)
    public UserShowFollowingsInFeedViewResponse showMyFollowingsInFeedView(Long userId) {
        // 1. 유저가 팔로잉하는 사람들을 팔로잉을 최근에 맺은 순으로 조회
        // TODO : 유저가 팔로잉하는 사람들이 너무 많으면??? -> 고민해봐야 함
        List<FollowingQueryDto> followingQueryDtos = followingQueryPort.findAllFollowingUsersOrderByFollowedAtDesc(userId);

        if (followingQueryDtos.isEmpty()) {
            return UserShowFollowingsInFeedViewResponse.returnEmptyList();
        }

        // 2. 유저가 팔로잉하는 사람들 중, 가장 최근에 공개 피드를 작성한 사람들을 조회
        List<Long> latestPublicFeedCreators = fetchLatestPublicFeedCreators(followingQueryDtos);

        // 3. 결과 조합 : 팔로잉 유저들 중, 최신 공개 피드 작성자 우선 -> 최근 팔로우 맺은 순
        LinkedHashSet<Long> orderedIds = assembleOrderedIds(latestPublicFeedCreators, followingQueryDtos);

        // 4. ID 순서대로 DTO 매핑하여 response 반환
        Map<Long, FollowingQueryDto> followingMap = followingQueryDtos.stream()
                .collect(Collectors.toMap(
                        FollowingQueryDto::followingTargetUserId,
                        dto -> dto
                ));
        List<FollowingQueryDto> result = orderedIds.stream()
                .map(followingMap::get)
                .toList();

        return new UserShowFollowingsInFeedViewResponse(userQueryMapper.toFollowingFeedViewDtos(result));
    }

    private static LinkedHashSet<Long> assembleOrderedIds(List<Long> latestPublicFeedCreators, List<FollowingQueryDto> followingQueryDtos) {
        LinkedHashSet<Long> orderedIds = new LinkedHashSet<>(latestPublicFeedCreators);

        if (orderedIds.size() < SIZE) {
            for (FollowingQueryDto dto : followingQueryDtos) {
                if (orderedIds.size() >= SIZE) break;
                orderedIds.add(dto.followingTargetUserId());
            }
        }
        return orderedIds;
    }

    private List<Long> fetchLatestPublicFeedCreators(List<FollowingQueryDto> followingQueryDtos) {
        Set<Long> followingUserIds = followingQueryDtos.stream()
                .map(FollowingQueryDto::followingTargetUserId)
                .collect(Collectors.toSet());

        return feedQueryPort.findLatestPublicFeedCreatorsIn(followingUserIds, SIZE);
    }
}
