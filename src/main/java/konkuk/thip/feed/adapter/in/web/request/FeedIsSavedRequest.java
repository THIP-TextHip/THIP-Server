package konkuk.thip.feed.adapter.in.web.request;

import jakarta.validation.constraints.NotNull;
import konkuk.thip.feed.application.port.in.dto.FeedIsSavedCommand;

public record FeedIsSavedRequest(
        @NotNull(message = "type은 필수입니다.")
        Boolean type
) {
    public static FeedIsSavedCommand toCommand(Long userId, Long feedId, Boolean type) {
        return new FeedIsSavedCommand(userId, feedId, type);
    }
}
