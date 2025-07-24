package konkuk.thip.feed.application.port.in;

import konkuk.thip.feed.adapter.in.web.response.FeedShowAllResponse;

public interface FeedShowAllUseCase {

    FeedShowAllResponse showAllFeeds(Long userId, String cursor);
}
