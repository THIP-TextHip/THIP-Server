package konkuk.thip.comment.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import konkuk.thip.comment.application.port.in.dto.CommentCreateCommand;

public record CommentCreateRequest(

        @NotBlank(message = "댓글 내용은 필수입니다.")
        String content,

        @NotNull(message = "답글 여부는 필수입니다.")
        Boolean isReplyRequest,

        Long parentId,

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
