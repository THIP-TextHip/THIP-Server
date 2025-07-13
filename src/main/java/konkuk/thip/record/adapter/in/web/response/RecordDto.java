package konkuk.thip.record.adapter.in.web.response;

import lombok.Builder;

@Builder
public record RecordDto(
        String postDate,
        int page,
        Long userId,
        String nickName,
        String profileImageUrl,
        String content,
        int likeCount,
        int commentCount,
        boolean isLiked,
        boolean isWriter,
        Long recordId
) implements RecordSearchResponse.RecordSearchResult {
    @Override
    public String type() {
        return "RECORD";
    }

    public RecordDto withIsLiked(boolean isLiked) {
        return new RecordDto(
                postDate,
                page,
                userId,
                nickName,
                profileImageUrl,
                content,
                likeCount,
                commentCount,
                isLiked,
                isWriter,
                recordId
        );
    }
}
