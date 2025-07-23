package konkuk.thip.feed.adapter.in.web.response;

import konkuk.thip.feed.application.port.in.dto.FeedIsSavedResult;

public record FeedIsSavedResponse(
        Long feedId,
        boolean isSaved
) {
        public static FeedIsSavedResponse of(FeedIsSavedResult feedIsSavedResult) {
                return new FeedIsSavedResponse(feedIsSavedResult.feedId(), feedIsSavedResult.isSaved());
        }
}
