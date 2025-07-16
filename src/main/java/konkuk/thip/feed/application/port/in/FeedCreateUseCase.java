package konkuk.thip.feed.application.port.in;

import konkuk.thip.feed.application.port.in.dto.FeedCreateCommand;

public interface FeedCreateUseCase {
    Long createFeed(FeedCreateCommand command);
}
