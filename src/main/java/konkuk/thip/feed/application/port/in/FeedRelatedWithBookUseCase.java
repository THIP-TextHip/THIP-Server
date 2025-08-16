package konkuk.thip.feed.application.port.in;

import konkuk.thip.feed.adapter.in.web.response.FeedRelatedWithBookResponse;
import konkuk.thip.feed.application.port.in.dto.FeedRelatedWithBookQuery;

public interface FeedRelatedWithBookUseCase {

    FeedRelatedWithBookResponse getFeedsByBook(FeedRelatedWithBookQuery query);
}
