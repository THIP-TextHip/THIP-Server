package konkuk.thip.feed.application.port.in;

import konkuk.thip.feed.adapter.in.web.response.FeedShowSingleResponse;

public interface FeedShowSingleUseCase {

    FeedShowSingleResponse showSingleFeed(Long feedId, Long userId);
}
