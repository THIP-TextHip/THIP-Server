package konkuk.thip.comment.adapter.in.web.request;

import jakarta.validation.constraints.NotNull;
import konkuk.thip.comment.application.port.in.dto.CommentIsLikeCommand;

public record CommentIsLikeRequest(
        @NotNull(message = "좋아요 여부는 필수입니다.")
        Boolean type
) {
    public static CommentIsLikeCommand toCommand(Long userId, Long commentId, Boolean type) {
        return new CommentIsLikeCommand(userId, commentId, type);
    }
}
