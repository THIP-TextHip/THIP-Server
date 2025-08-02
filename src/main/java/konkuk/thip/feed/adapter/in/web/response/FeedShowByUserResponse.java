package konkuk.thip.feed.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "특정 유저의 피드 목록 조회 응답 DTO")
public record FeedShowByUserResponse(
        @Schema(description = "조회된 피드 목록")
        List<FeedShowByUserResponse.FeedDto> feedList,

        @Schema(description = "다음 페이지를 요청할 때 사용할 커서")
        String nextCursor,

        @Schema(description = "마지막 페이지 여부")
        boolean isLast
) {
    @Schema(description = "피드 단일 항목 정보")
    public record FeedDto(
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
            boolean isSaved,
            boolean isLiked
    ) { }
}
