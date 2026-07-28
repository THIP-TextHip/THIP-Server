package konkuk.thip.comment.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import konkuk.thip.comment.application.port.in.dto.ChildCommentCreateCommand;

@Schema(description = "자식 댓글(답글) 작성 요청 DTO")
public record ChildCommentCreateRequest(

        @Schema(description = "댓글 내용", example = "좋은 의견이네요!")
        @NotBlank(message = "댓글 내용은 필수입니다.")
        String content,

        @Schema(description = "게시물 타입 (RECORD, VOTE, FEED)", example = "RECORD")
        @NotBlank(message = "게시물 타입은 필수입니다.")
        String postType,

        @Schema(description = "게시물 ID", example = "1")
        @NotNull(message = "게시물 ID는 필수입니다.")
        Long postId
) {
        public ChildCommentCreateCommand toCommand(Long userId, Long parentCommentId) {
                return new ChildCommentCreateCommand(content, postType, postId, userId, parentCommentId);
        }
}
