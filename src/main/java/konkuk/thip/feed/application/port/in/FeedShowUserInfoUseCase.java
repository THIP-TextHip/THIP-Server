package konkuk.thip.feed.application.port.in;

import konkuk.thip.feed.adapter.in.web.response.FeedShowUserInfoResponse;

public interface FeedShowUserInfoUseCase {

    FeedShowUserInfoResponse showMyInfoInFeeds(Long userId);

    FeedShowUserInfoResponse showAnotherUserInfoInFeeds(Long userId, Long feedOwnerId);
}
