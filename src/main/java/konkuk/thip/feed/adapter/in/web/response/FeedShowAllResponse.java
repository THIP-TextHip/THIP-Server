package konkuk.thip.feed.adapter.in.web.response;

import java.util.List;

public record FeedShowAllResponse(
        List<FeedDto> feedList,
        String nextCursor,
        boolean isLast
) {
    public record FeedDto(
            Long feedId,
            Long creatorId,
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
            boolean isLiked,
            boolean isWriter
    ) { }
}
