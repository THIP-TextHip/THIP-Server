package konkuk.thip.feed.application.port.in;

import konkuk.thip.feed.adapter.in.web.response.FeedShowSavedListResponse;

public interface FeedSavedListUseCase {
    FeedShowSavedListResponse getSavedFeedList(Long userId, String cursor);
}
