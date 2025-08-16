package konkuk.thip.feed.adapter.in.web.response;

import lombok.Builder;

import java.util.List;

@Builder
public record FeedRelatedWithBookResponse(
       List<FeedRelatedWithBookDto> feeds,
       String nextCursor,
       boolean isLast
) {
    public record FeedRelatedWithBookDto(
            Long feedId,
            Long creatorId,
            boolean isWriter,
            String creatorNickname,
            String creatorProfileImageUrl,
            String aliasName,
            String aliasColor,
            String postDate,
            String isbn,
            String bookTitle,
            String bookAuthor,
            String contentBody,
            String[] contentUrls,
            int likeCount,
            int commentCount,
            boolean isSaved,
            boolean isLiked
    ) {}

    public static FeedRelatedWithBookResponse of(List<FeedRelatedWithBookDto> feeds, String nextCursor, boolean isLast) {
        return new FeedRelatedWithBookResponse(feeds, nextCursor, isLast);
    }
}
