package konkuk.thip.feed.application.port.in;

import konkuk.thip.feed.application.port.in.dto.FeedIsSavedCommand;
import konkuk.thip.feed.application.port.in.dto.FeedIsSavedResult;

public interface FeedSavedUseCase {
    FeedIsSavedResult changeSavedFeed(FeedIsSavedCommand feedIsSavedCommand);
}
