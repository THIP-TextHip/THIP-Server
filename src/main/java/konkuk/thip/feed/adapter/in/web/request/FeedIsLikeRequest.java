package konkuk.thip.feed.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import konkuk.thip.post.application.port.in.dto.PostIsLikeCommand;

import static konkuk.thip.common.post.PostType.FEED;

@Schema(description = "피드 좋아요 상태 변경 요청 DTO")
public record FeedIsLikeRequest(
        @Schema(description = "좋아요 여부 type (true -> 좋아요, false -> 좋아요 취소)", example = "true")
        @NotNull(message = "좋아요 여부는 필수입니다.")
        Boolean type
) {
    public PostIsLikeCommand toCommand(Long userId, Long feedId) {
        return new PostIsLikeCommand(userId, feedId, FEED ,type);
    }
}