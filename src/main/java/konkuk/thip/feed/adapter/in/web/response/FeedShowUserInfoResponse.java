package konkuk.thip.feed.adapter.in.web.response;

import lombok.Builder;

import java.util.List;

@Builder
public record FeedShowUserInfoResponse(
        Long creatorId,
        String profileImageUrl,
        String nickname,
        String aliasName,
        String aliasColor,
        int followerCount,
        int totalFeedCount,
        boolean isFollowing,
        List<String> latestFollowerProfileImageUrls
) { }
