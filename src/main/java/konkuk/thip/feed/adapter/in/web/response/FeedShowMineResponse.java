package konkuk.thip.feed.adapter.in.web.response;

import java.util.List;

public record FeedShowMineResponse(
        List<FeedShowMineDto> feedList,
        String nextCursor,
        boolean isLast
) {
    public record FeedShowMineDto(
            Long feedId,
            String postDate,
            String isbn,
            String bookTitle,
            String bookAuthor,
            String contentBody,
            String[] contentUrls,
            int likeCount,
            int commentCount,
            boolean isPublic,
            boolean isWriter
    ) { }
}
