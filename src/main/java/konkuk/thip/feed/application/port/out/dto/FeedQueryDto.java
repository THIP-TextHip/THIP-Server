package konkuk.thip.feed.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;
import jakarta.annotation.Nullable;
import konkuk.thip.feed.domain.value.ContentList;
import konkuk.thip.user.domain.value.Alias;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FeedQueryDto(
        Long feedId,
        Long creatorId,
        String creatorNickname,
        String creatorProfileImageUrl,
        String alias,
        LocalDateTime createdAt,
        String isbn,
        String bookTitle,
        String bookAuthor,
        String contentBody,
        String[] contentUrls,
        Integer likeCount,
        Integer commentCount,
        boolean isPublic,
        @Nullable Boolean isPriorityFeed,
        @Nullable LocalDateTime savedCreatedAt
) {
    @QueryProjection
    public FeedQueryDto(
            Long feedId,
            Long creatorId,
            String creatorNickname,
            Alias alias,
            LocalDateTime createdAt,
            String isbn,
            String bookTitle,
            String bookAuthor,
            String contentBody,
            ContentList contentList,
            Integer likeCount,
            Integer commentCount,
            Boolean isPublic,
            @Nullable Boolean isPriorityFeed,
            @Nullable LocalDateTime savedCreatedAt
    ) {
        this(
                feedId,
                creatorId,
                creatorNickname,
                alias.getImageUrl(),
                alias.getValue(),
                createdAt,
                isbn,
                bookTitle,
                bookAuthor,
                contentBody,
                convertToArray(contentList),
                likeCount == null ? 0 : likeCount,
                commentCount == null ? 0 : commentCount,
                isPublic,
                isPriorityFeed,
                savedCreatedAt
        );
    }

    private static String[] convertToArray(ContentList contentList) {
        if (contentList == null || contentList.isEmpty()) {
            return new String[0];
        }
        return contentList.toArray(String[]::new);
    }

}
