package konkuk.thip.feed.application.port.in;

import konkuk.thip.feed.adapter.in.web.response.FeedShowMineResponse;

public interface FeedShowMineUseCase {

    FeedShowMineResponse showMyFeeds(Long userId, String cursor);
}
