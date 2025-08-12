package konkuk.thip.comment.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import konkuk.thip.comment.application.port.in.dto.CommentIsLikeCommand;

@Schema(description = "댓글 좋아요 상태 변경 요청 DTO")
public record CommentIsLikeRequest(
        @Schema(description = "좋아요 여부 type (true -> 좋아요, false -> 좋아요 취소)", example = "true")
        @NotNull(message = "좋아요 여부는 필수입니다.")
        Boolean type
) {
    public CommentIsLikeCommand toCommand(Long userId, Long commentId) {
        return new CommentIsLikeCommand(userId, commentId, type);
    }
}
