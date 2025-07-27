package konkuk.thip.feed.application.port.out.dto;

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
        int likeCount,
        int commentCount
) { }
