package konkuk.thip.feed.application.service;

import konkuk.thip.feed.adapter.in.web.response.FeedShowUserInfoResponse;
import konkuk.thip.feed.application.mapper.FeedQueryMapper;
import konkuk.thip.feed.application.port.in.FeedShowUserInfoUseCase;
import konkuk.thip.feed.application.port.out.FeedQueryPort;
import konkuk.thip.user.application.port.out.FollowingQueryPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class FeedShowUserInfoService implements FeedShowUserInfoUseCase {

    private static final int FOLLOWER_DISPLAY_LIMIT = 5;
    private final UserCommandPort userCommandPort;
    private final FollowingQueryPort followingQueryPort;
    private final FeedQueryPort feedQueryPort;
    private final FeedQueryMapper feedQueryMapper;

    @Transactional(readOnly = true)
    @Override
    public FeedShowUserInfoResponse showMyInfoInFeeds(Long userId) {
        // 1. User 찾기
        User feedOwner = userCommandPort.findById(userId);

        // 2. 해당 유저를 팔로우 하는 유저들을 프로필 이미지 정보 구하기
        List<String> latestFollowerProfileImageUrls = followingQueryPort.getLatestFollowerImageUrls(userId, FOLLOWER_DISPLAY_LIMIT);

        // 3. 내가 작성한 전체 피드 개수 구하기
        int allFeedCount = feedQueryPort.countAllFeedsByUserId(userId);

        return feedQueryMapper.toFeedShowUserInfoResponse(feedOwner, allFeedCount, false, latestFollowerProfileImageUrls);
    }

    @Transactional(readOnly = true)
    @Override
    public FeedShowUserInfoResponse showAnotherUserInfoInFeeds(Long userId, Long feedOwnerId) {
        // 1. feedOwner 찾기
        User feedOwner = userCommandPort.findById(feedOwnerId);

        // 2. feedOwner를 팔로우 하는 유저들을 프로필 이미지 정보 구하기
        List<String> latestFollowerProfileImageUrls = followingQueryPort.getLatestFollowerImageUrls(feedOwnerId, FOLLOWER_DISPLAY_LIMIT);

        // 3. feedOwner가 작성한 공개 피드 개수 구하기
        int publicFeedCount = feedQueryPort.countPublicFeedsByUserId(feedOwnerId);

        // 4. user가 feedOwner 를 팔로잉하는지 조회
        boolean isFollowing = followingQueryPort.isFollowingUser(userId, feedOwnerId);

        return feedQueryMapper.toFeedShowUserInfoResponse(feedOwner, publicFeedCount, isFollowing, latestFollowerProfileImageUrls);
    }
}
