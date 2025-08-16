package konkuk.thip.feed.application.port.in.dto;

import lombok.Builder;

@Builder
public record FeedRelatedWithBookQuery(
        String isbn,
        FeedRelatedWithBookSortType sortType,
        String cursor,
        Long userId
) {
}
