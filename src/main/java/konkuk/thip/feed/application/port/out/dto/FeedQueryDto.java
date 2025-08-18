package konkuk.thip.feed.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;
import jakarta.annotation.Nullable;
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
            String creatorProfileImageUrl,
            String alias,
            LocalDateTime createdAt,
            String isbn,
            String bookTitle,
            String bookAuthor,
            String contentBody,
            String concatenatedContentUrls, // GROUP_CONCAT 결과
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
                creatorProfileImageUrl,
                alias,
                createdAt,
                isbn,
                bookTitle,
                bookAuthor,
                contentBody,
                splitToArray(concatenatedContentUrls),
                likeCount == null ? 0 : likeCount,
                commentCount == null ? 0 : commentCount,
                isPublic,
                isPriorityFeed,
                savedCreatedAt
        );
    }

    // Attribute convertor로 바꾸기 전에 임시 메서드
    private static String[] splitToArray(String concatenated) {
        if (concatenated == null || concatenated.isEmpty()) return new String[0];
        String[] parts = concatenated.split(",");
        for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();
        return parts;
    }

}
