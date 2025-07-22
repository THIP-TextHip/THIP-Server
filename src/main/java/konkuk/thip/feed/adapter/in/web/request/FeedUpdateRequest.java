package konkuk.thip.feed.adapter.in.web.request;

import konkuk.thip.feed.application.port.in.dto.FeedUpdateCommand;

import java.util.List;

public record FeedUpdateRequest(

        String contentBody,

        Boolean isPublic,

        List<String> tagList,

        List<String> remainImageUrls
) {
        public FeedUpdateCommand toCommand(Long userId, Long feedId) {
                return new FeedUpdateCommand(
                        contentBody,
                        isPublic,
                        tagList,
                        remainImageUrls,
                        userId,
                        feedId
                );
        }
}
