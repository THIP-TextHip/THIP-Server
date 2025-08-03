package konkuk.thip.feed.adapter.in.web.response;

public record FeedShowSingleResponse(
        Long feedId,
        Long creatorId,
        String creatorNickname,
        String creatorProfileImageUrl,
        String alias,
        String aliasColor,
        String postDate,
        String isbn,
        String bookAuthor,
        String contentBody,
        String[] contentUrls,
        int likeCount,
        int commentCount,
        boolean isSaved,
        boolean isLiked,
        String[] tagList
) {
}
