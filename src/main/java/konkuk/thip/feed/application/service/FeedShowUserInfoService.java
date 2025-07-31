package konkuk.thip.feed.application.service;

import konkuk.thip.feed.adapter.in.web.response.FeedShowUserInfoResponse;
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

    @Transactional(readOnly = true)
    @Override
    public FeedShowUserInfoResponse showMyInfoInFeeds(Long userId) {
        // 1. User 찾기
        User feedOwner = userCommandPort.findById(userId);

        // 2. 해당 유저를 팔로우 하는 유저들을 프로필 이미지 정보 구하기
        List<String> latestFollowerProfileImageUrls = followingQueryPort.getLatestFollowerImageUrls(userId, FOLLOWER_DISPLAY_LIMIT);

        // 3. 내가 작성한 전체 피드 개수 구하기
        int allFeedCount = feedQueryPort.countAllFeedsByUserId(userId);

        return buildResponse(feedOwner, allFeedCount, latestFollowerProfileImageUrls);
    }

    @Transactional(readOnly = true)
    @Override
    public FeedShowUserInfoResponse showAnotherUserInfoInFeeds(Long anotherUserId) {
        // 1. User 찾기
        User feedOwner = userCommandPort.findById(anotherUserId);

        // 2. 해당 유저를 팔로우 하는 유저들을 프로필 이미지 정보 구하기
        List<String> latestFollowerProfileImageUrls = followingQueryPort.getLatestFollowerImageUrls(anotherUserId, FOLLOWER_DISPLAY_LIMIT);

        // 3. 유저가 작성한 공개 피드 개수 구하기
        int publicFeedCount = feedQueryPort.countPublicFeedsByUserId(anotherUserId);

        return buildResponse(feedOwner, publicFeedCount, latestFollowerProfileImageUrls);
    }

    private FeedShowUserInfoResponse buildResponse(User feedOwner, int totalFeedCount, List<String> latestFollowerProfileImageUrls) {
        return FeedShowUserInfoResponse.builder()
                .profileImageUrl(feedOwner.getAlias().getImageUrl())
                .nickname(feedOwner.getNickname())
                .aliasName(feedOwner.getAlias().getValue())
                .aliasColor(feedOwner.getAlias().getColor())
                .followerCount(feedOwner.getFollowerCount())
                .totalFeedCount(totalFeedCount)
                .latestFollowerProfileImageUrls(latestFollowerProfileImageUrls)
                .build();
    }
}
