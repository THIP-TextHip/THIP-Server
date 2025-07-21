package konkuk.thip.feed.application.port.in;

import konkuk.thip.feed.application.port.in.dto.FeedUpdateCommand;

public interface FeedUpdateUseCase {
    Long updateFeed(FeedUpdateCommand command);
}