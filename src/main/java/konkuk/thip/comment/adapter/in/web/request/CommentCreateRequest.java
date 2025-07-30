package konkuk.thip.comment.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import konkuk.thip.comment.application.port.in.dto.CommentCreateCommand;

@Schema(description = "댓글 작성 요청 DTO")
public record CommentCreateRequest(

        @Schema(description = "댓글 내용", example = "이 게시물 정말 좋아요!")
        @NotBlank(message = "댓글 내용은 필수입니다.")
        String content,

        @Schema(description = "답글 여부", example = "true")
        @NotNull(message = "답글 여부는 필수입니다.")
        Boolean isReplyRequest,

        @Schema(description = "답글의 부모 댓글 ID (답글이 아닐 경우 null)", example = "1")
        Long parentId,

        @Schema(description = "게시물 타입 (RECORD, VOTE, FEED)", example = "RECORD")
        @NotBlank(message = "게시물 타입은 필수입니다.")
        String postType

) {
        public CommentCreateCommand toCommand(Long userId, Long postId) {
                return new CommentCreateCommand(
                        content,
                        isReplyRequest,
                        parentId,
                        postType,
                        postId,
                        userId
                );
        }
}
