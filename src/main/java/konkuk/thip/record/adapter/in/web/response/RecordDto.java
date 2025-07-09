package konkuk.thip.record.adapter.in.web.response;

import konkuk.thip.record.domain.Record;
import konkuk.thip.user.domain.User;
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
) implements RecordSearchResponse.PostDto {
    @Override
    public String type() {
        return "RECORD";
    }

    public static RecordDto of(Record record, String postDate, User user, int likeCount, int commentCount, boolean isLiked, boolean isWriter) {
        return RecordDto.builder()
                .postDate(postDate)
                .page(record.getPage())
                .userId(record.getCreatorId())
                .nickName(user.getNickname())
                .profileImageUrl(user.getImageUrl())
                .content(record.getContent())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .isLiked(isLiked)
                .isWriter(isWriter)
                .recordId(record.getId())
                .build();
    }
}
