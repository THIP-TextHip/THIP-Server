package konkuk.thip.feed.adapter.in.web.response;

import lombok.Builder;

import java.util.List;

@Builder
public record FeedShowUserInfoResponse(
        String profileImageUrl,
        String nickname,
        String aliasName,
        String aliasColor,
        int followerCount,
        int totalFeedCount,
        List<String> latestFollowerProfileImageUrls
) { }
