package konkuk.thip.record.adapter.in.web.response;

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
) implements RecordSearchResponse.PostDto {
    @Override
    public String type() {
        return "RECORD";
    }
}
