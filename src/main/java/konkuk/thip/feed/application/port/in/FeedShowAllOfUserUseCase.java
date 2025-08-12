package konkuk.thip.feed.application.port.in;

import konkuk.thip.feed.adapter.in.web.response.FeedShowByUserResponse;
import konkuk.thip.feed.adapter.in.web.response.FeedShowMineResponse;

public interface FeedShowAllOfUserUseCase {

    FeedShowMineResponse showMyFeeds(Long userId, String cursor);

    FeedShowByUserResponse showPublicFeedsOfFeedOwner(Long userId, Long feedOwnerId, String cursor);
}
